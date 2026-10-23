package com.seriando.app.model

import com.google.gson.annotations.SerializedName

// ---------- Respostas da TMDB (append_to_response) ----------

/** Filmes: release_dates, uma entrada por país com as datas e certificações de lançamento. */
data class ReleaseDatesResponse(
    @SerializedName("results") val resultados: List<ReleaseDatesPais> = emptyList()
)

data class ReleaseDatesPais(
    @SerializedName("iso_3166_1") val pais: String = "",
    @SerializedName("release_dates") val datas: List<ReleaseDateItem> = emptyList()
)

/** Um lançamento do filme no país; a certificação vem vazia em vários tipos de lançamento. */
data class ReleaseDateItem(
    @SerializedName("certification") val certificacao: String? = null
)

/** Séries: content_ratings, uma classificação por país. */
data class ContentRatingsResponse(
    @SerializedName("results") val resultados: List<ContentRatingItem> = emptyList()
)

data class ContentRatingItem(
    @SerializedName("iso_3166_1") val pais: String = "",
    @SerializedName("rating") val classificacao: String? = null
)

/**
 * Classificação indicativa do Brasil (RF49, RN14): L, 10, 12, 14, 16 ou 18.
 * Só a região BR é considerada, e valores fora dessa lista são ignorados.
 *
 * @param rotulo      texto mostrado no selo ("L", "10", "12", "14", "16" ou "18").
 * @param idadeMinima idade mínima em anos (0 para "L"); usada nos filtros do RF50 e RF51.
 */
data class ClassificacaoIndicativa(
    val rotulo: String,
    val idadeMinima: Int
) {
    companion object {
        // Rótulos válidos do Brasil e a idade de cada um
        private val IDADES = mapOf("L" to 0, "10" to 10, "12" to 12, "14" to 14, "16" to 16, "18" to 18)

        /** Converte o texto da TMDB em classificação; null se vazio ou fora da lista do Brasil. */
        private fun deTexto(texto: String?): ClassificacaoIndicativa? {
            val limpo = texto?.trim()?.uppercase() ?: return null
            val idade = IDADES[limpo] ?: return null
            return ClassificacaoIndicativa(limpo, idade)
        }

        /** Classificação de um filme no Brasil: a mais restritiva entre os lançamentos de BR. */
        fun doFilme(resposta: ReleaseDatesResponse?): ClassificacaoIndicativa? =
            resposta?.resultados.orEmpty()
                .filter { it.pais == "BR" }
                .flatMap { it.datas }
                .mapNotNull { deTexto(it.certificacao) }
                .maxByOrNull { it.idadeMinima }

        /** Classificação de uma série no Brasil. */
        fun daSerie(resposta: ContentRatingsResponse?): ClassificacaoIndicativa? =
            resposta?.resultados.orEmpty()
                .filter { it.pais == "BR" }
                .mapNotNull { deTexto(it.classificacao) }
                .maxByOrNull { it.idadeMinima }
    }
}