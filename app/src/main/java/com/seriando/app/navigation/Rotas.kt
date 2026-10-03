package com.seriando.app.navigation

object Rotas {
    const val HOME = "home"
    const val BUSCA = "busca"
    const val DETALHES = "detalhes/{tipoMidia}/{serieId}"

    fun detalhesComId(tipoMidia: String, serieId: Int) = "detalhes/$tipoMidia/$serieId"
}