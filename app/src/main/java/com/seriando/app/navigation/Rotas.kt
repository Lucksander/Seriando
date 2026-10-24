package com.seriando.app.navigation

// Nomes das rotas (telas) do app, centralizados para evitar erros de digitação.
object Rotas {
    const val INTRODUCAO = "introducao" // 3 telas de boas-vindas, só na primeira abertura (RF52)
    const val ENTRADA = "entrada" // login com Google ou visitante, em toda abertura sem sessão (RF53)
    const val HOME = "home"
    const val BUSCA = "busca"
    const val DETALHES = "detalhes/{tipoMidia}/{serieId}"
    const val PESSOA = "pessoa/{pessoaId}" // página do ator/atriz (RF40)
    const val CATEGORIA = "categoria/{categoriaId}" // lista completa de uma categoria (RF46)
    const val TERMOS = "termos" // Termos de Uso, texto dentro do app (abre offline e para visitante)
    const val PRIVACIDADE = "privacidade" // Política de Privacidade, texto dentro do app

    fun detalhesComId(tipoMidia: String, serieId: Int) = "detalhes/$tipoMidia/$serieId"
    fun pessoaComId(pessoaId: Int) = "pessoa/$pessoaId"
    fun categoriaComId(categoriaId: String) = "categoria/$categoriaId"
}