package com.seriando.app.model

/**
 * Próximo episódio a assistir de uma série: temporada e número do episódio (RF83).
 */
data class ProximoEpisodio(val temporada: Int, val numero: Int)

/**
 * Um item da fileira "Continuar assistindo" da Home (RF83): uma série com status "Assistindo"
 * e o quanto já foi visto dela.
 *
 * @param serie      a série da lista pessoal (nome, capa e id).
 * @param assistidos quantos episódios o usuário já marcou como assistidos.
 * @param total      total de episódios da série; 0 quando os detalhes ainda não estão guardados
 *                   no aparelho (nesse caso a barra de progresso não é mostrada).
 * @param proximo    próximo episódio a assistir; nulo quando a série está em dia ou quando os
 *                   detalhes não estão guardados.
 */
data class ContinuarAssistindo(
    val serie: SerieSalva,
    val assistidos: Int,
    val total: Int,
    val proximo: ProximoEpisodio?
) {
    /** Progresso de 0f a 1f para a barra; 0f se o total for desconhecido. */
    val progresso: Float
        get() = if (total > 0) (assistidos.toFloat() / total).coerceIn(0f, 1f) else 0f
}

/**
 * Regras puras (sem Android, sem banco) da fileira "Continuar assistindo".
 * Ficam separadas para poderem ser testadas com testes unitários simples.
 */
object ContinuarAssistindoRegras {

    // Quantas séries a fileira mostra no máximo
    const val LIMITE_DA_FILEIRA = 10

    /**
     * Descobre o próximo episódio a assistir: o seguinte ao último assistido (maior temporada
     * e, dentro dela, maior número). Se o último era o final da temporada, vai para o episódio
     * 1 da próxima temporada. Sem nenhum episódio assistido, é o episódio 1 da primeira
     * temporada. A temporada 0 (especiais) é ignorada.
     *
     * Observação: lacunas antigas (um episódio pulado no meio) não são consideradas; a fileira
     * "continua de onde parou", não procura buracos.
     *
     * @param assistidos pares (temporada, número) dos episódios assistidos.
     * @param temporadas temporadas da série, com a quantidade de episódios de cada uma.
     * @return o próximo episódio, ou null se a série está em dia (ou sem temporadas).
     */
    fun proximoEpisodio(
        assistidos: Collection<Pair<Int, Int>>,
        temporadas: List<TemporadaResumo>
    ): ProximoEpisodio? {
        // Só temporadas regulares e com episódios, em ordem crescente
        val validas = temporadas
            .filter { it.numero > 0 && it.totalEpisodios > 0 }
            .sortedBy { it.numero }
        if (validas.isEmpty()) return null

        // Último episódio assistido (ignora especiais); sem nenhum, começa do início
        val ultimo = assistidos
            .filter { it.first > 0 }
            .maxWithOrNull(compareBy({ it.first }, { it.second }))
            ?: return ProximoEpisodio(validas.first().numero, 1)

        val (temporadaVista, numeroVisto) = ultimo

        // Ainda há episódio depois do último, na mesma temporada
        val atual = validas.firstOrNull { it.numero == temporadaVista }
        if (atual != null && numeroVisto < atual.totalEpisodios) {
            return ProximoEpisodio(temporadaVista, numeroVisto + 1)
        }

        // Senão, o primeiro episódio da temporada seguinte (ou null se não há mais)
        val seguinte = validas.firstOrNull { it.numero > temporadaVista } ?: return null
        return ProximoEpisodio(seguinte.numero, 1)
    }
}
