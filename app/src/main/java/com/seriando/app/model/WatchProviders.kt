package com.seriando.app.model

import com.google.gson.annotations.SerializedName

// Resposta do endpoint watch/providers da TMDB: um mapa de código de país
// (ex: "BR") para os provedores disponíveis naquele país.
data class WatchProvidersResponse(
    val results: Map<String, ProvedoresPais> = emptyMap()
)

data class ProvedoresPais(
    val link: String? = null, // página da TMDB com todos os detalhes (JustWatch por trás)
    @SerializedName("flatrate") val assinatura: List<Provedor>? = null,
    @SerializedName("rent") val aluguel: List<Provedor>? = null,
    @SerializedName("buy") val compra: List<Provedor>? = null
)

data class Provedor(
    @SerializedName("provider_id") val id: Int,
    @SerializedName("provider_name") val nome: String,
    @SerializedName("logo_path") val caminhoLogo: String?
)