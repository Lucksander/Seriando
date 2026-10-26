package com.seriando.app.model

/**
 * Regra do carrossel de destaques da Home (RF84): quais obras entram e em que ordem.
 * Fica separada da tela (sem Android) para poder ser testada com testes unitários simples.
 */
object DestaquesDaHome {

    // Quantas obras de cada tipo entram no carrossel (total = 2 × este valor)
    const val POR_TIPO = 3

    /**
     * Pega as primeiras obras de "Séries em alta" e de "Filmes em alta" que têm imagem de
     * fundo e as alterna: série, filme, série, filme... Se um dos lados tiver menos itens,
     * o outro completa o restante. Não faz nenhuma requisição: usa as listas que a Home já
     * carregou, que já passaram pelo filtro por idade (RF50).
     *
     * @param series obras da fileira "Séries em alta".
     * @param filmes obras da fileira "Filmes em alta".
     */
    fun escolher(series: List<ResultadoBusca>, filmes: List<ResultadoBusca>): List<ResultadoBusca> {
        // Só entram obras com imagem horizontal; sem ela o cartão ficaria vazio
        val s = series.filter { it.caminhoFundo != null }.take(POR_TIPO)
        val f = filmes.filter { it.caminhoFundo != null }.take(POR_TIPO)

        // Alterna os dois lados e, no fim, acrescenta o que sobrou do maior
        val resultado = mutableListOf<ResultadoBusca>()
        val tamanho = maxOf(s.size, f.size)
        for (i in 0 until tamanho) {
            s.getOrNull(i)?.let { resultado.add(it) }
            f.getOrNull(i)?.let { resultado.add(it) }
        }
        return resultado
    }
}
