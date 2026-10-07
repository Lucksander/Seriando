package com.seriando.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.seriando.app.model.Denuncia
import kotlinx.coroutines.tasks.await

// Repositório de denúncias (RF28). Só grava — a leitura é restrita
// a quem administra o app, fora do escopo deste cliente Android.
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
        db.collection("denuncias").add(denuncia).await()
    }
}