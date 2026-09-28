package com.seriando.app.data.remote

import com.seriando.app.model.SerieDetalhe
import com.seriando.app.model.TmdbSearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Interface que descreve as chamadas à API da TMDB.
// O Retrofit gera a implementação real por trás dessas anotações.
interface TmdbApiService {

    // Pesquisa séries por nome (usado na SearchScreen).
    @GET("search/tv")
    suspend fun buscarSeries(
        @Query("api_key") apiKey: String,
        @Query("query") nomeSerie: String,
        @Query("language") idioma: String = "pt-BR"
    ): TmdbSearchResponse

    // Busca os detalhes completos de uma série específica pelo ID.
    // {id} na URL é substituído pelo valor de serieId, graças ao @Path.
    @GET("tv/{id}")
    suspend fun buscarDetalhesSerie(
        @Path("id") serieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") idioma: String = "pt-BR"
    ): SerieDetalhe
}