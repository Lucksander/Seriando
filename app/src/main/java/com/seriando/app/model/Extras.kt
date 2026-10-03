package com.seriando.app.model

import com.google.gson.annotations.SerializedName

// Elenco (vem dentro do campo "credits" da resposta, graças ao append_to_response).
data class Creditos(
    val cast: List<ElencoItem> = emptyList()
)

data class ElencoItem(
    val id: Int,
    val name: String,
    @SerializedName("character") val personagem: String? = null,
    @SerializedName("profile_path") val caminhoFoto: String? = null
)

// Vídeos/trailers (vem dentro do campo "videos").
data class VideosResponse(
    val results: List<VideoItem> = emptyList()
)

data class VideoItem(
    val key: String, // ID do vídeo no YouTube
    val site: String, // normalmente "YouTube"
    val type: String, // "Trailer", "Teaser" etc.
    val name: String
)