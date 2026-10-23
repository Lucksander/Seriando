package com.seriando.app.model

import com.google.gson.annotations.SerializedName

/**
 * Detalhes de um filme vindos da TMDB (movie/{id}).
 * situacao é opcional: cópias antigas do Room não a têm e chegam como null.
 */
data class MovieDetalhe(
    val id: Int,
    @SerializedName("title") val nome: String,
    @SerializedName("overview") val sinopse: String,
    @SerializedName("poster_path") val caminhoCapa: String?,
    @SerializedName("release_date") val dataLancamento: String?,
    @SerializedName("runtime") val duracaoMinutos: Int?,
    @SerializedName("vote_average") val notaMedia: Double = 0.0,
    val credits: Creditos? = null,
    val videos: VideosResponse? = null,
    // Situação do filme na TMDB: "Released", "Post Production", "In Production", "Planned"...
    @SerializedName("status") val situacao: String? = null,
    // Datas e certificações por país (append_to_response=release_dates); usada para a do Brasil (RF49)
    @SerializedName("release_dates") val datasDeLancamento: ReleaseDatesResponse? = null
)