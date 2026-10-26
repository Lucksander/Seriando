package com.seriando.app.model

import com.google.gson.annotations.SerializedName

/**
 * Detalhes de uma série vindos da TMDB (tv/{id}).
 * Os campos novos (situacao e dataUltimoEpisodio) são opcionais: cópias antigas
 * guardadas no Room (obrasCache) não os têm e simplesmente chegam como null.
 */
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
    val videos: VideosResponse? = null,
    // Situação da série na TMDB: "Returning Series", "Ended", "Canceled", "In Production"...
    @SerializedName("status") val situacao: String? = null,
    // Data do último episódio exibido; usada para o ano final do intervalo ("2019 – 2024")
    @SerializedName("last_air_date") val dataUltimoEpisodio: String? = null,
    // Classificações por país (append_to_response=content_ratings); usada para a do Brasil (RF49)
    @SerializedName("content_ratings") val classificacoes: ContentRatingsResponse? = null,
    // "Onde assistir" por país (append_to_response=watch/providers). Opcional: cópias antigas
    // do Room não o têm, e então a tela usa a chamada separada como reserva.
    @SerializedName("watch/providers") val provedores: WatchProvidersResponse? = null

)

/**
 * Resumo de uma temporada dentro dos detalhes da série.
 * dataExibicao (air_date) é opcional e serve para mostrar o ano no cartão da temporada.
 */
data class TemporadaResumo(
    @SerializedName("season_number") val numero: Int,
    @SerializedName("episode_count") val totalEpisodios: Int,
    @SerializedName("name") val nome: String,
    @SerializedName("air_date") val dataExibicao: String? = null
)