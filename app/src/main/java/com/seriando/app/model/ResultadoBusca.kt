package com.seriando.app.model

import com.google.gson.annotations.SerializedName

// Representa um item devolvido pela busca multi (search/multi) da TMDB,
// que mistura séries, filmes e pessoas num resultado só.
// Série usa os campos "name"/"first_air_date"; filme usa "title"/"release_date" —
// por isso os dois pares de campo existem aqui, e só um deles vem preenchido
// por vez, dependendo do tipoMidia.
data class ResultadoBusca(
    val id: Int,
    @SerializedName("media_type") val tipoMidia: String? = null, // "movie", "tv" ou "person"
    @SerializedName("name") private val nomeSerie: String? = null,
    @SerializedName("title") private val tituloFilme: String? = null,
    @SerializedName("overview") val sinopse: String = "",
    @SerializedName("poster_path") val caminhoCapa: String? = null,
    @SerializedName("first_air_date") private val dataEstreiaSerie: String? = null,
    @SerializedName("release_date") private val dataLancamentoFilme: String? = null
) {
    // Nome "unificado", não importa se é série ou filme.
    val nome: String get() = nomeSerie ?: tituloFilme ?: "Sem título"

    // Data "unificada", idem.
    val data: String? get() = dataEstreiaSerie ?: dataLancamentoFilme

    val ehFilme: Boolean get() = tipoMidia == "movie"
    val ehSerie: Boolean get() = tipoMidia == "tv"
}

// Resposta do endpoint search/multi: mesma estrutura de "envelope"
// que já usávamos, só que com ResultadoBusca em vez de Serie.
data class BuscaMultiResponse(
    val page: Int,
    val results: List<ResultadoBusca>,
    @SerializedName("total_pages") val totalPaginas: Int = 1
)