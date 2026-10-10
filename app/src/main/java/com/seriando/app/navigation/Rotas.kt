package com.seriando.app.navigation

// Nomes das rotas (telas) do app, centralizados para evitar erros de digitação.
object Rotas {
    const val HOME = "home"
    const val BUSCA = "busca"
    const val DETALHES = "detalhes/{tipoMidia}/{serieId}"
    const val PESSOA = "pessoa/{pessoaId}" // página do ator/atriz (RF40)
    const val CATEGORIA = "categoria/{categoriaId}" // lista completa de uma categoria (RF46)

    fun detalhesComId(tipoMidia: String, serieId: Int) = "detalhes/$tipoMidia/$serieId"
    fun pessoaComId(pessoaId: Int) = "pessoa/$pessoaId"
    fun categoriaComId(categoriaId: String) = "categoria/$categoriaId"
}