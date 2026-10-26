package com.seriando.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.seriando.app.model.Denuncia
import kotlinx.coroutines.tasks.await

// Repositório de denúncias (RF28). Só grava — a leitura é restrita
// a quem administra o app, fora do escopo deste cliente Android.
// RF68: ao denunciar, também grava uma marca de ocultação na conta de quem
// denunciou (usuarios/{uid}/opinioesOcultas), lida por AvaliacaoRepository.
class DenunciaRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun denunciarOpiniao(
        autorOpiniaoUid: String,
        tipoMidia: String,
        tmdbId: Int,
        denuncianteUid: String,
        motivo: String
    ) {
        val denuncia = Denuncia(
            autorOpiniaoUid = autorOpiniaoUid,
            tipoMidia = tipoMidia,
            tmdbId = tmdbId,
            denuncianteUid = denuncianteUid,
            motivo = motivo
        )
        // RF68: a denúncia e a marca de ocultação vão no MESMO lote (tudo ou nada).
        // Assim a opinião nunca fica denunciada e ainda visível para quem denunciou,
        // nem oculta sem que a denúncia tenha sido registrada.
        val lote = db.batch()

        // Denúncia em denuncias/{id} (id gerado pelo Firestore), como antes
        lote.set(db.collection("denuncias").document(), denuncia)

        // Marca em usuarios/{denuncianteUid}/opinioesOcultas/{obra_autor}.
        // O id do documento combina obra e autor, então denunciar de novo
        // a mesma opinião apenas regrava a mesma marca, sem duplicar.
        val chaveObra = "${tipoMidia}_$tmdbId"
        val marca = mapOf(
            "obraKey" to chaveObra,                      // obra onde a opinião está (para filtrar)
            "autorUid" to autorOpiniaoUid,               // autor da opinião ocultada
            "ocultadaEm" to System.currentTimeMillis()   // quando foi ocultada
        )
        lote.set(
            db.collection("usuarios").document(denuncianteUid)
                .collection("opinioesOcultas").document("${chaveObra}_$autorOpiniaoUid"),
            marca
        )

        lote.commit().await()
    }
}