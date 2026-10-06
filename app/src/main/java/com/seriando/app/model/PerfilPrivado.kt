package com.seriando.app.model

// Dados privados do usuário, visíveis só para ele mesmo (RNF03).
// Fica em usuarios/{uid}, no mesmo documento que já guarda a subcoleção "series".
data class PerfilPrivado(
    val email: String = "",
    val aniversario: String? = null // formato livre por enquanto, ex: "15/03"
)