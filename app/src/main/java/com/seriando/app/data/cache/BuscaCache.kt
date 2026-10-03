package com.seriando.app.data.cache

import com.seriando.app.model.ResultadoBusca

// Guarda em memória o texto e os resultados da última pesquisa feita.
// Como é um "object" (singleton), esse valor sobrevive enquanto o app
// está aberto, mesmo navegando para outras telas e voltando — diferente
// do "remember" dentro de um Composable, que se perde quando a tela
// é recriada ao navegar de volta.
object BuscaCache {
    var textoPesquisa: String = ""
    var resultados: List<ResultadoBusca> = emptyList()
}