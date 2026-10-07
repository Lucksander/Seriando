package com.seriando.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.seriando.app.model.Avaliacao
import com.seriando.app.model.VotoResumo
import kotlinx.coroutines.tasks.await

class AvaliacaoRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun idObra(tipoMidia: String, tmdbId: Int) = "${tipoMidia}_$tmdbId"

    private fun colecaoOpinioes(tipoMidia: String, tmdbId: Int) =
        db.collection("avaliacoes").document(idObra(tipoMidia, tmdbId)).collection("opinioes")

    // Votos de uma opinião específica: avaliacoes/{obra}/opinioes/{autorUid}/votos/{votanteUid}
    private fun colecaoVotos(autorOpiniaoUid: String, tipoMidia: String, tmdbId: Int) =
        colecaoOpinioes(tipoMidia, tmdbId).document(autorOpiniaoUid).collection("votos")

    suspend fun salvarNota(uid: String, tipoMidia: String, tmdbId: Int, nota: Int) {
        val dados = mapOf("autorUid" to uid, "nota" to nota, "data" to System.currentTimeMillis())
        colecaoOpinioes(tipoMidia, tmdbId).document(uid).set(dados, SetOptions.merge()).await()
    }

    suspend fun salvarOpiniao(
        uid: String,
        tipoMidia: String,
        tmdbId: Int,
        texto: String,
        spoiler: Boolean,
        emocoes: List<String>,
        publica: Boolean,
        apelidoAutor: String,
        avatarAutor: String
    ) {
        val dados = mapOf(
            "autorUid" to uid,
            "apelidoAutor" to apelidoAutor,
            "avatarAutor" to avatarAutor,
            "texto" to texto,
            "spoiler" to spoiler,
            "emocoes" to emocoes,
            "publica" to publica,
            "data" to System.currentTimeMillis()
        )
        colecaoOpinioes(tipoMidia, tmdbId).document(uid).set(dados, SetOptions.merge()).await()
    }

    suspend fun removerOpiniao(uid: String, tipoMidia: String, tmdbId: Int) {
        val dados = mapOf(
            "texto" to "",
            "spoiler" to false,
            "emocoes" to emptyList<String>(),
            "publica" to true
        )
        colecaoOpinioes(tipoMidia, tmdbId).document(uid).set(dados, SetOptions.merge()).await()
    }

    suspend fun buscarMinhaAvaliacao(uid: String, tipoMidia: String, tmdbId: Int): Avaliacao? {
        val documento = colecaoOpinioes(tipoMidia, tmdbId).document(uid).get().await()
        return if (documento.exists()) documento.toObject(Avaliacao::class.java) else null
    }

    suspend fun removerMinhaAvaliacao(uid: String, tipoMidia: String, tmdbId: Int) {
        colecaoOpinioes(tipoMidia, tmdbId).document(uid).delete().await()
    }

    suspend fun buscarTodasAvaliacoes(tipoMidia: String, tmdbId: Int): List<Avaliacao> {
        val resultado = colecaoOpinioes(tipoMidia, tmdbId).get().await()
        return resultado.toObjects(Avaliacao::class.java)
    }

    suspend fun calcularMedia(tipoMidia: String, tmdbId: Int): Double {
        val avaliacoes = buscarTodasAvaliacoes(tipoMidia, tmdbId).filter { it.nota > 0 }
        if (avaliacoes.isEmpty()) return 0.0
        return avaliacoes.map { it.nota }.average()
    }

    suspend fun buscarOpinioes(tipoMidia: String, tmdbId: Int, uidAtual: String?): List<Avaliacao> {
        return buscarTodasAvaliacoes(tipoMidia, tmdbId)
            .filter { it.texto.isNotBlank() && (it.publica || it.autorUid == uidAtual) }
            .sortedByDescending { it.data }
    }

    // RF23, RF24, RF25: registra o voto do usuário numa opinião. Tocar de
    // novo no mesmo valor remove o voto (toggle), em vez de criar um novo.
    // RN04 (não votar na própria opinião) é checado por quem chama esta função.
    suspend fun votar(autorOpiniaoUid: String, tipoMidia: String, tmdbId: Int, votanteUid: String, valor: Int) {
        val documento = colecaoVotos(autorOpiniaoUid, tipoMidia, tmdbId).document(votanteUid)
        val atual = documento.get().await()
        val valorAtual = if (atual.exists()) atual.getLong("valor")?.toInt() else null

        if (valorAtual == valor) {
            documento.delete().await() // mesmo voto de novo: desfaz
        } else {
            documento.set(mapOf("valor" to valor)).await() // voto novo, ou trocando de curtir para descurtir (ou vice-versa)
        }
    }

    // RF26: conta curtidas/descurtidas de uma opinião, e identifica o voto
    // do usuário atual, para destacar o botão já escolhido.
    suspend fun buscarResumoVotos(
        autorOpiniaoUid: String,
        tipoMidia: String,
        tmdbId: Int,
        votanteUidAtual: String?
    ): VotoResumo {
        val todos = colecaoVotos(autorOpiniaoUid, tipoMidia, tmdbId).get().await()
        var curtidas = 0
        var descurtidas = 0
        var meuVoto: Int? = null
        todos.documents.forEach { doc ->
            val valor = doc.getLong("valor")?.toInt() ?: return@forEach
            if (valor == 1) curtidas++ else if (valor == -1) descurtidas++
            if (doc.id == votanteUidAtual) meuVoto = valor
        }
        return VotoResumo(curtidas, descurtidas, meuVoto)
    }
}