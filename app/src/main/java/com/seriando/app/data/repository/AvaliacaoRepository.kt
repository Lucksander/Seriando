package com.seriando.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.seriando.app.model.Avaliacao
import kotlinx.coroutines.tasks.await

class AvaliacaoRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun idObra(tipoMidia: String, tmdbId: Int) = "${tipoMidia}_$tmdbId"

    private fun colecaoOpinioes(tipoMidia: String, tmdbId: Int) =
        db.collection("avaliacoes").document(idObra(tipoMidia, tmdbId)).collection("opinioes")

    suspend fun salvarNota(uid: String, tipoMidia: String, tmdbId: Int, nota: Int) {
        val dados = mapOf("autorUid" to uid, "nota" to nota, "data" to System.currentTimeMillis())
        colecaoOpinioes(tipoMidia, tmdbId).document(uid).set(dados, SetOptions.merge()).await()
    }

    // RF19, RF20, RF22 (editar), RF35: salva/atualiza texto, spoiler,
    // emoções e visibilidade, sem alterar a nota salva separadamente.
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

    // RF22 (remover): limpa a opinião por completo (texto, spoiler,
    // emoções, visibilidade), preservando a nota, que tem seu próprio
    // botão de remoção (RF17), separado deste.
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

    // RF21: lista as opiniões com texto, visíveis para quem está olhando.
    // Uma opinião privada só aparece para o próprio autor (uidAtual).
    suspend fun buscarOpinioes(tipoMidia: String, tmdbId: Int, uidAtual: String?): List<Avaliacao> {
        return buscarTodasAvaliacoes(tipoMidia, tmdbId)
            .filter { it.texto.isNotBlank() && (it.publica || it.autorUid == uidAtual) }
            .sortedByDescending { it.data }
    }
}