package com.seriando.app.data.repository


import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.seriando.app.model.Avaliacao
import kotlinx.coroutines.tasks.await

// Repositório de avaliações e opiniões sobre obras (filmes/séries).
// Caminho: avaliacoes/{tipoMidia_tmdbId}/opinioes/{autorUid}
class AvaliacaoRepository {

    private val db = FirebaseFirestore.getInstance()

    // Monta o ID da obra juntando tipo e ID da TMDB, para não colidir
    // um filme e uma série que por acaso tenham o mesmo ID numérico.
    private fun idObra(tipoMidia: String, tmdbId: Int) = "${tipoMidia}_$tmdbId"

    private fun colecaoOpinioes(tipoMidia: String, tmdbId: Int) =
        db.collection("avaliacoes").document(idObra(tipoMidia, tmdbId)).collection("opinioes")

    // Salva/atualiza só a nota do usuário atual sobre a obra (RF16, RF17).
    // merge() preserva o texto da opinião, caso já exista (Bloco C).
    suspend fun salvarNota(uid: String, tipoMidia: String, tmdbId: Int, nota: Int) {
        val dados = mapOf("autorUid" to uid, "nota" to nota, "data" to System.currentTimeMillis())
        colecaoOpinioes(tipoMidia, tmdbId).document(uid).set(dados, SetOptions.merge()).await()
    }

    // Busca a avaliação do próprio usuário sobre essa obra, se existir.
    suspend fun buscarMinhaAvaliacao(uid: String, tipoMidia: String, tmdbId: Int): Avaliacao? {
        val documento = colecaoOpinioes(tipoMidia, tmdbId).document(uid).get().await()
        return if (documento.exists()) documento.toObject(Avaliacao::class.java) else null
    }

    // Remove a nota do usuário (RF17). Se o documento também tiver uma
    // opinião em texto (Bloco C), o documento inteiro é removido — já que
    // RN01 trata nota e opinião como a mesma entrada do usuário na obra.
    suspend fun removerMinhaAvaliacao(uid: String, tipoMidia: String, tmdbId: Int) {
        colecaoOpinioes(tipoMidia, tmdbId).document(uid).delete().await()
    }

    // Busca todas as avaliações da obra, para calcular a média (RF18).
    suspend fun buscarTodasAvaliacoes(tipoMidia: String, tmdbId: Int): List<Avaliacao> {
        val resultado = colecaoOpinioes(tipoMidia, tmdbId).get().await()
        return resultado.toObjects(Avaliacao::class.java)
    }

    // Calcula a média das notas (ignorando documentos sem nota, ex: uma
    // opinião em texto sem nota marcada, se isso vier a ser permitido).
    suspend fun calcularMedia(tipoMidia: String, tmdbId: Int): Double {
        val avaliacoes = buscarTodasAvaliacoes(tipoMidia, tmdbId).filter { it.nota > 0 }
        if (avaliacoes.isEmpty()) return 0.0
        return avaliacoes.map { it.nota }.average()
    }
}