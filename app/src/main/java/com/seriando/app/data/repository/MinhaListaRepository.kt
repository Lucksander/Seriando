package com.seriando.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.seriando.app.model.SerieSalva
import kotlinx.coroutines.tasks.await

/**
 * Lista pessoal do usuário (RF07, RF08): usuarios/{uid}/series/{serieId}.
 * As funções não tratam erros: quem chama (a tela) faz try/catch.
 */
class MinhaListaRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun colecaoSeries(uid: String) =
        db.collection("usuarios").document(uid).collection("series")

    suspend fun adicionarSerie(uid: String, serie: SerieSalva) {
        colecaoSeries(uid).document(serie.id.toString()).set(serie).await()
    }

    /**
     * Remove o item da lista JUNTO com o progresso de episódios dele (RF29).
     * O Firestore não apaga subcoleções sozinho: sem isto, os episódios assistidos
     * ficariam no banco sem pai, invisíveis para o app e impossíveis de apagar depois.
     * Ordem: primeiro os episódios (filhos), depois o documento da série (pai).
     * Se falhar no meio, o pai continua existindo e uma nova tentativa termina o serviço.
     */
    suspend fun removerSerie(uid: String, serieId: Int) {
        val documentoSerie = colecaoSeries(uid).document(serieId.toString())

        // 1) Progresso de episódios, em lotes (o Firestore aceita até 500 operações por lote)
        val episodios = documentoSerie.collection("episodiosAssistidos").get().await().documents
        episodios.chunked(400).forEach { grupo ->
            val lote = db.batch()
            grupo.forEach { lote.delete(it.reference) }
            lote.commit().await()
        }

        // 2) Só depois o documento da série
        documentoSerie.delete().await()
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