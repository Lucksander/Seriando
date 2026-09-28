package com.seriando.app.model

import com.google.gson.annotations.SerializedName

data class Serie(
    val id: Int,
    @SerializedName("name") val nome: String,
    @SerializedName("overview") val sinopse: String,
    @SerializedName("poster_path") val caminhoCapa: String?,
    @SerializedName("first_air_date") val dataEstreia: String?
)

data class TmdbSearchResponse(
    val page: Int,
    val results: List<Serie>,
    @SerializedName("total_results") val totalResultados: Int
)