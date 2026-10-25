package com.seriando.app.model

import java.text.Normalizer

/**
 * Ordem em que a Minha Lista é mostrada (RF66).
 *
 * @param rotulo texto mostrado no menu de ordenação.
 */
enum class OrdemDaLista(val rotulo: String) {
    /** Os adicionados por último primeiro (ordem padrão, a mesma de antes do RF66). */
    RECENTES("Mais recentes"),

    /** Ordem alfabética pelo nome, ignorando maiúsculas e acentos. */
    NOME("Nome (A a Z)"),

    /** Agrupa por status, na ordem do ciclo de acompanhamento; dentro de cada status, por nome. */
    STATUS("Status")
}

/**
 * Aplica à lista, nesta ordem: filtro de status, busca por nome e ordenação (RF66).
 * É uma função pura (sem Android), então dá para testá-la na JVM.
 *
 * @param lista       itens da lista pessoal.
 * @param filtroLabel rótulo do status escolhido nos chips ("Quero assistir", "Maratonei"...);
 *                    null mostra todos.
 * @param busca       texto digitado; vazio não filtra. Ignora maiúsculas, acentos e espaços nas pontas.
 * @param ordem       ordem do resultado.
 */
fun organizarMinhaLista(
    lista: List<SerieSalva>,
    filtroLabel: String?,
    busca: String,
    ordem: OrdemDaLista
): List<SerieSalva> {
    val textoBusca = semAcentoEMinusculo(busca.trim())

    val filtrada = lista
        // 1) status: compara pelo rótulo, que é o que o chip guarda
        .filter { filtroLabel == null || labelDoStatus(it.tipoMidia, it.status) == filtroLabel }
        // 2) nome: contém o texto buscado (busca vazia deixa todos passarem)
        .filter { textoBusca.isEmpty() || semAcentoEMinusculo(it.nome).contains(textoBusca) }

    return when (ordem) {
        OrdemDaLista.RECENTES -> filtrada.sortedByDescending { it.dataAdicao }
        OrdemDaLista.NOME -> filtrada.sortedBy { semAcentoEMinusculo(it.nome) }
        // Primeiro pela posição do status no ciclo, depois pelo nome
        OrdemDaLista.STATUS -> filtrada.sortedWith(
            compareBy<SerieSalva> { posicaoDoStatus(it.status) }
                .thenBy { semAcentoEMinusculo(it.nome) }
        )
    }
}

/**
 * Posição do status no ciclo de acompanhamento, para ordenar por status:
 * quero assistir, assistindo, terminado (maratonei ou concluído), abandonei.
 * Status desconhecido vai para o começo, como "quero assistir" (o mesmo padrão do app).
 */
private fun posicaoDoStatus(status: String): Int = when (status) {
    "QUERO_ASSISTIR" -> 0
    "ASSISTINDO" -> 1
    "COMPLETO", "ASSISTIDO" -> 2
    "ABANDONEI" -> 3
    else -> 0
}

/** Deixa o texto em minúsculas e sem acentos ("Ação" vira "acao"), para comparar sem se importar com isso. */
private fun semAcentoEMinusculo(texto: String): String =
    Normalizer.normalize(texto, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "") // remove as marcas de acento separadas pelo NFD
        .lowercase()
