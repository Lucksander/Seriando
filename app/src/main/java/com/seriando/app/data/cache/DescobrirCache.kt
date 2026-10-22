package com.seriando.app.data.cache

import com.seriando.app.model.CatalogoDescoberta
import com.seriando.app.model.ResultadoBusca
import com.seriando.app.model.TipoDescoberta

/**
 * Estado da aba Descobrir guardado em memória (não persistido), no mesmo estilo
 * do AbaCache e do BuscaCache.
 *
 * A tela da aba é destruída quando o usuário abre os Detalhes ou troca de aba.
 * Ao sair, a DescobrirScreen grava aqui os filtros, os resultados já carregados e a
 * posição da rolagem; ao voltar, restaura tudo, sem nova chamada de rede.
 */
object DescobrirCache {
    // Filtros escolhidos nos três menus
    var tipo: TipoDescoberta = TipoDescoberta.FILMES
    var catalogo: CatalogoDescoberta = CatalogoDescoberta.Popular
    var generoId: Int? = null // null = "Todos"

    // Resultados já carregados e o ponto da paginação
    var itens: List<ResultadoBusca> = emptyList()
    var paginaAtual: Int = 0   // última página carregada (0 = nenhuma)
    var totalPaginas: Int = 0  // limite informado pela TMDB

    // Posição da rolagem da grade
    var indicePrimeiroItem: Int = 0
    var deslocamento: Int = 0
}