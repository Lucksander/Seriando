package com.seriando.app.model

// Representa um episódio marcado como assistido pelo usuário.
// temporada + numero juntos identificam o episódio de forma única.
data class EpisodioAssistido(
    val temporada: Int = 0,
    val numero: Int = 0,
    val dataAssistido: Long = System.currentTimeMillis()
)