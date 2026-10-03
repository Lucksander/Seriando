package com.seriando.app.model

// Representa o documento de perfil do usuário no Firestore
// (usuarios/{uid}), separado da subcoleção "series".
data class PerfilUsuario(
    val apelido: String = "",
    val email: String = ""
)