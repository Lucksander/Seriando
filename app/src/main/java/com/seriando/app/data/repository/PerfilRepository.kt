package com.seriando.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.seriando.app.model.PerfilUsuario
import kotlinx.coroutines.tasks.await

// Repositório do documento de perfil do usuário (nome de exibição no app).
class PerfilRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun documentoPerfil(uid: String) =
        db.collection("usuarios").document(uid)

    // Busca o perfil salvo. Retorna null se o usuário nunca teve um
    // documento de perfil criado (ex: primeiro login).
    suspend fun buscarPerfil(uid: String): PerfilUsuario? {
        val documento = documentoPerfil(uid).get().await()
        return if (documento.exists()) documento.toObject(PerfilUsuario::class.java) else null
    }

    // Salva/atualiza só os campos do perfil, sem apagar a subcoleção "series"
    // que já existe dentro do mesmo documento de usuário.
    suspend fun salvarPerfil(uid: String, perfil: PerfilUsuario) {
        // merge() garante que só os campos passados aqui são escritos,
        // sem sobrescrever o documento inteiro (a subcoleção "series" é
        // uma coleção à parte, mas por segurança usamos merge de qualquer forma).
        documentoPerfil(uid).set(perfil, SetOptions.merge()).await()
    }
}