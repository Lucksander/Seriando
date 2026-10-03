package com.seriando.app.model

data class SerieSalva(
    val id: Int = 0,
    val nome: String = "",
    val sinopse: String = "",
    val caminhoCapa: String? = null,
    val tipoMidia: String = "tv",
    val status: String = "QUERO_ASSISTIR", // interpretado via StatusSerie ou StatusFilme, conforme tipoMidia
    val dataAdicao: Long = System.currentTimeMillis()
)