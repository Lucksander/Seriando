package com.seriando.app.model

// Dados públicos do usuário, visíveis para qualquer um da comunidade
// (ex: ao lado de uma opinião, no Bloco C). Fica em perfisPublicos/{uid}.
data class PerfilPublico(
    val apelido: String = "",
    // "google" usa a foto da conta Google; qualquer outro valor é um emoji
    // escolhido como avatar dentro do próprio app (RF15).
    val avatarId: String = "google"
)