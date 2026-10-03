package com.seriando.app.data.cache

// Guarda qual aba (Home ou Minha Lista) estava selecionada, pelo mesmo
// motivo do BuscaCache: o MainScaffold perde seu estado de aba quando
// a tela de Detalhes é aberta por cima dele e depois se volta.
object AbaCache {
    var abaSelecionada: Int = 0
}