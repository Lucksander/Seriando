package com.seriando.app.model

import com.google.gson.annotations.SerializedName

data class TemporadaDetalhe(
    @SerializedName("episodes") val episodios: List<EpisodioResumo>
)

data class EpisodioResumo(
    val id: Int,
    @SerializedName("episode_number") val numero: Int,
    @SerializedName("name") val nome: String,
    @SerializedName("still_path") val caminhoThumbnail: String? = null // imagem do episódio (RF47); pode vir nula
)