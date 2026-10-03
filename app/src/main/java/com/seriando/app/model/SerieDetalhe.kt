package com.seriando.app.model

import com.google.gson.annotations.SerializedName

data class SerieDetalhe(
    val id: Int,
    @SerializedName("name") val nome: String,
    @SerializedName("overview") val sinopse: String,
    @SerializedName("poster_path") val caminhoCapa: String?,
    @SerializedName("first_air_date") val dataEstreia: String?,
    @SerializedName("number_of_seasons") val numeroTemporadas: Int,
    @SerializedName("number_of_episodes") val numeroEpisodios: Int,
    @SerializedName("vote_average") val notaMedia: Double = 0.0,
    @SerializedName("seasons") val temporadas: List<TemporadaResumo> = emptyList(),
    val credits: Creditos? = null,
    val videos: VideosResponse? = null
)

data class TemporadaResumo(
    @SerializedName("season_number") val numero: Int,
    @SerializedName("episode_count") val totalEpisodios: Int,
    @SerializedName("name") val nome: String
)