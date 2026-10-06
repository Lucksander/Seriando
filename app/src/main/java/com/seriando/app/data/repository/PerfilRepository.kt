package com.seriando.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.seriando.app.model.PerfilPrivado
import com.seriando.app.model.PerfilPublico
import kotlinx.coroutines.tasks.await

// Repositório do perfil do usuário, separado em dois documentos conforme
// o modelo de dados do projeto: perfisPublicos/{uid} e usuarios/{uid}.
class PerfilRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun docPublico(uid: String) = db.collection("perfisPublicos").document(uid)
    private fun docPrivado(uid: String) = db.collection("usuarios").document(uid)

    suspend fun buscarPerfilPublico(uid: String): PerfilPublico? {
        val documento = docPublico(uid).get().await()
        return if (documento.exists()) documento.toObject(PerfilPublico::class.java) else null
    }

    // merge() evita sobrescrever campos que não foram passados aqui.
    suspend fun salvarPerfilPublico(uid: String, perfil: PerfilPublico) {
        docPublico(uid).set(perfil, SetOptions.merge()).await()
    }

    suspend fun buscarPerfilPrivado(uid: String): PerfilPrivado? {
        val documento = docPrivado(uid).get().await()
        return if (documento.exists()) documento.toObject(PerfilPrivado::class.java) else null
    }

    // merge() aqui é essencial: o documento usuarios/{uid} também é o "pai"
    // da subcoleção "series" — sem merge, salvar o perfil privado poderia
    // sobrescrever o documento inteiro.
    suspend fun salvarPerfilPrivado(uid: String, perfil: PerfilPrivado) {
        docPrivado(uid).set(perfil, SetOptions.merge()).await()
    }
}