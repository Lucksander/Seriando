package com.seriando.app.model

/**
 * Lógica pura das recomendações da Home (RF81). Fica em model/ para ser testada na JVM,
 * sem Android e sem rede: quem busca na TMDB é o CatalogoRepository.
 */
object Recomendacoes {

    /** Quantas obras da lista servem de base para pedir recomendações à TMDB. */
    const val QUANTIDADE_DE_BASES = 5

    /** Tamanho máximo da fileira "Recomendados para você". */
    const val LIMITE_DA_FILEIRA = 20

    /**
     * Escolhe as obras da lista que servem de base: as que o usuário já terminou
     * (séries "Maratonei" = COMPLETO, filmes "Concluído" = ASSISTIDO), da mais recente
     * para a mais antiga pela data em que entraram na lista.
     *
     * @param lista  lista pessoal inteira.
     * @param maximo quantas bases devolver.
     */
    fun escolherBases(lista: List<SerieSalva>, maximo: Int = QUANTIDADE_DE_BASES): List<SerieSalva> =
        lista
            .filter { (it.tipoMidia == "tv" && it.status == "COMPLETO") || (it.tipoMidia == "movie" && it.status == "ASSISTIDO") }
            .sortedByDescending { it.dataAdicao }
            .take(maximo)

    /**
     * Junta as recomendações de várias obras-base em uma fileira só:
     *  - tira as repetidas (mesmo tipo + id);
     *  - tira as obras que já estão na lista do usuário (inclui as próprias bases);
     *  - ordena por quantas bases recomendaram a obra (mais votos primeiro); no empate,
     *    vale a ordem em que apareceu (a TMDB já devolve as mais relevantes primeiro);
     *  - corta no [limite].
     *
     * @param recomendacoesPorBase uma lista de recomendações para cada obra-base, na ordem das bases.
     * @param jaNaLista            obras que o usuário já tem, como pares (tipo, id).
     */
    fun combinar(
        recomendacoesPorBase: List<List<ResultadoBusca>>,
        jaNaLista: Set<Pair<String, Int>>,
        limite: Int = LIMITE_DA_FILEIRA
    ): List<ResultadoBusca> {
        // LinkedHashMap preserva a ordem da primeira aparição, que serve de desempate
        val votos = LinkedHashMap<Pair<String, Int>, Int>()
        val obras = LinkedHashMap<Pair<String, Int>, ResultadoBusca>()

        recomendacoesPorBase.forEach { lista ->
            // distinctBy: uma base só vota uma vez em cada obra
            lista.distinctBy { (it.tipoMidia ?: "") to it.id }.forEach { obra ->
                val chave = (obra.tipoMidia ?: "") to obra.id
                if (chave in jaNaLista) return@forEach
                votos[chave] = (votos[chave] ?: 0) + 1
                obras.putIfAbsent(chave, obra)
            }
        }

        // sortedByDescending é estável: no empate mantém a ordem de chegada
        return obras.keys
            .sortedByDescending { votos.getValue(it) }
            .take(limite)
            .map { obras.getValue(it) }
    }
}
