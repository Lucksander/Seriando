package com.seriando.app.model

import com.google.gson.annotations.SerializedName

// Resposta do endpoint tv/{id}/season/{numero}: a lista completa
// de episódios daquela temporada.
data class TemporadaDetalhe(
    @SerializedName("episodes") val episodios: List<EpisodioResumo>
)

data class EpisodioResumo(
    val id: Int,
    @SerializedName("episode_number") val numero: Int,
    @SerializedName("name") val nome: String
)