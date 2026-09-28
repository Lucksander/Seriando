package com.seriando.app.navigation

// Objeto que guarda os nomes das rotas (telas) do app.
// Usar constantes aqui evita erros de digitação ao navegar.
object Rotas {
    const val HOME = "home"
    const val BUSCA = "busca"

    // Rota de detalhes recebe o ID da série como parâmetro,
    // por isso o "{serieId}" faz parte do próprio nome da rota.
    const val DETALHES = "detalhes/{serieId}"

    // Função auxiliar que monta a rota já com o ID substituído,
    // usada na hora de navegar (ex: detalhesComId(1399) -> "detalhes/1399").
    fun detalhesComId(serieId: Int) = "detalhes/$serieId"
}