package com.seriando.app.model

data class SerieSalva(
    val id: Int = 0,
    val nome: String = "",
    val sinopse: String = "",
    val caminhoCapa: String? = null,
    val tipoMidia: String = "tv",
    val status: String = "QUERO_ASSISTIR", // interpretado via StatusSerie ou StatusFilme, conforme tipoMidia
    val dataAdicao: Long = System.currentTimeMillis(),
    // Instante da última alteração (milissegundos). A sincronização usa este valor para
    // fazer valer a gravação mais recente (RN23). Documentos antigos da nuvem não têm este
    // campo e valem 0, ou seja, sempre perdem para uma alteração local.
    val atualizadoEm: Long = 0L
)