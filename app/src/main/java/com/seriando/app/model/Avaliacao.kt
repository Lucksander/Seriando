package com.seriando.app.model

data class Avaliacao(
    val autorUid: String = "",
    val apelidoAutor: String = "",
    val avatarAutor: String = "google",
    val nota: Int = 0,
    val texto: String = "",
    val spoiler: Boolean = false,
    val emocoes: List<String> = emptyList(),
    val publica: Boolean = true, // false = só o autor vê a própria opinião
    val data: Long = System.currentTimeMillis()
)