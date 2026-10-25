package com.seriando.app.model

/**
 * Números da aba Conta (RF67), calculados a partir da lista pessoal e do progresso salvos
 * no banco local do usuário. Não são gravados em lugar nenhum: são contados na hora.
 *
 * @param filmesConcluidos    filmes da lista com o status "Concluído".
 * @param seriesMaratonadas   séries da lista com o status "Maratonei".
 * @param episodiosAssistidos total de episódios marcados como assistidos, em todas as séries.
 */
data class EstatisticasDoUsuario(
    val filmesConcluidos: Int = 0,
    val seriesMaratonadas: Int = 0,
    val episodiosAssistidos: Int = 0
)
