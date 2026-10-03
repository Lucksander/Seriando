package com.seriando.app.model

import com.google.gson.annotations.SerializedName

data class MovieDetalhe(
    val id: Int,
    @SerializedName("title") val nome: String,
    @SerializedName("overview") val sinopse: String,
    @SerializedName("poster_path") val caminhoCapa: String?,
    @SerializedName("release_date") val dataLancamento: String?,
    @SerializedName("runtime") val duracaoMinutos: Int?,
    @SerializedName("vote_average") val notaMedia: Double = 0.0,
    val credits: Creditos? = null,
    val videos: VideosResponse? = null
)