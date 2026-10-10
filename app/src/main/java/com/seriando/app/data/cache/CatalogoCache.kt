package com.seriando.app.data.cache

import com.seriando.app.model.ResultadoBusca

/**
 * Guarda em memória a primeira página de cada categoria da Home (RF44, RF45).
 * Segue o mesmo padrão do BuscaCache e do AbaCache: um "object" (singleton)
 * que sobrevive enquanto o app está aberto. Assim, ao abrir Detalhes ou a tela
 * "Ver mais" e voltar, a Home não precisa refazer as chamadas à TMDB nem
 * piscar em estado de carregamento.
 */
object CatalogoCache {

    // Chave: id da categoria (ex.: "acao"). Valor: itens da 1ª página, já com tipoMidia definido.
    private val primeirasPaginas = mutableMapOf<String, List<ResultadoBusca>>()

    /** Devolve os itens em cache da categoria, ou null se ainda não foram carregados. */
    fun obter(categoriaId: String): List<ResultadoBusca>? = primeirasPaginas[categoriaId]

    /** Guarda os itens da 1ª página de uma categoria. */
    fun guardar(categoriaId: String, itens: List<ResultadoBusca>) {
        primeirasPaginas[categoriaId] = itens
    }

    /** Apaga todo o cache. Chamar no logout (RF27), junto com a limpeza dos outros caches. */
    fun limpar() {
        primeirasPaginas.clear()
    }
}