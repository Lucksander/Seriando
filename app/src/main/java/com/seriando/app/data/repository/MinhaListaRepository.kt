package com.seriando.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.seriando.app.model.SerieSalva
import kotlinx.coroutines.tasks.await

class MinhaListaRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun colecaoSeries(uid: String) =
        db.collection("usuarios").document(uid).collection("series")

    suspend fun adicionarSerie(uid: String, serie: SerieSalva) {
        colecaoSeries(uid).document(serie.id.toString()).set(serie).await()
    }

    suspend fun removerSerie(uid: String, serieId: Int) {
        colecaoSeries(uid).document(serieId.toString()).delete().await()
    }

    suspend fun buscarSeries(uid: String): List<SerieSalva> {
        val resultado = colecaoSeries(uid).get().await()
        return resultado.toObjects(SerieSalva::class.java)
    }

    suspend fun contemSerie(uid: String, serieId: Int): Boolean {
        val documento = colecaoSeries(uid).document(serieId.toString()).get().await()
        return documento.exists()
    }

    // Atualiza só o campo "status" de um item já salvo, sem reescrever o documento inteiro.
    // Recebe o nome do enum já pronto (StatusSerie ou StatusFilme, resolvido pela tela).
    suspend fun atualizarStatusTexto(uid: String, serieId: Int, nomeStatus: String) {
        colecaoSeries(uid).document(serieId.toString())
            .update("status", nomeStatus)
            .await()
    }
}