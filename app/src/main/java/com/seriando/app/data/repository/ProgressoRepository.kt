package com.seriando.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.seriando.app.model.EpisodioAssistido
import com.seriando.app.model.EpisodioResumo
import kotlinx.coroutines.tasks.await

class ProgressoRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun colecaoAssistidos(uid: String, serieId: Int) =
        db.collection("usuarios").document(uid)
            .collection("series").document(serieId.toString())
            .collection("episodiosAssistidos")

    private fun idEpisodio(temporada: Int, numero: Int) = "${temporada}_$numero"

    suspend fun marcarAssistido(uid: String, serieId: Int, temporada: Int, numero: Int) {
        val episodio = EpisodioAssistido(temporada = temporada, numero = numero)
        colecaoAssistidos(uid, serieId)
            .document(idEpisodio(temporada, numero))
            .set(episodio)
            .await()
    }

    suspend fun desmarcarAssistido(uid: String, serieId: Int, temporada: Int, numero: Int) {
        colecaoAssistidos(uid, serieId)
            .document(idEpisodio(temporada, numero))
            .delete()
            .await()
    }

    suspend fun buscarAssistidos(uid: String, serieId: Int): Set<String> {
        val resultado = colecaoAssistidos(uid, serieId).get().await()
        return resultado.documents.map { it.id }.toSet()
    }

    // Marca todos os episódios de uma temporada de uma vez, usando um "batch"
    // (lote) do Firestore: todas as gravações acontecem juntas, numa única
    // operação, em vez de uma chamada separada por episódio.
    suspend fun marcarTemporadaComoAssistida(
        uid: String,
        serieId: Int,
        numeroTemporada: Int,
        episodios: List<EpisodioResumo>
    ) {
        val colecao = colecaoAssistidos(uid, serieId)
        val lote = db.batch()
        episodios.forEach { episodio ->
            val documento = colecao.document(idEpisodio(numeroTemporada, episodio.numero))
            lote.set(documento, EpisodioAssistido(temporada = numeroTemporada, numero = episodio.numero))
        }
        lote.commit().await()
    }
}