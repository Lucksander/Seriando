package com.seriando.app.model

import com.google.gson.annotations.SerializedName

// Representa a resposta do endpoint de detalhes da TMDB (tv/{id}),
// que traz mais informações do que a busca simples (Serie.kt) devolve.
data class SerieDetalhe(
    val id: Int,
    @SerializedName("name") val nome: String,
    @SerializedName("overview") val sinopse: String,
    @SerializedName("poster_path") val caminhoCapa: String?,
    @SerializedName("first_air_date") val dataEstreia: String?,
    @SerializedName("number_of_seasons") val numeroTemporadas: Int,
    @SerializedName("number_of_episodes") val numeroEpisodios: Int
)