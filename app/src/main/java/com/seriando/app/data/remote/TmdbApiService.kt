package com.seriando.app.data.remote

import com.seriando.app.model.BuscaMultiResponse
import com.seriando.app.model.MovieDetalhe
import com.seriando.app.model.SerieDetalhe
import com.seriando.app.model.TemporadaDetalhe
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import com.seriando.app.model.WatchProvidersResponse

interface TmdbApiService {

    // Onde assistir (RF34): disponibilidade de uma série por país.
    @GET("tv/{id}/watch/providers")
    suspend fun buscarProvedoresSerie(
        @Path("id") serieId: Int,
        @Query("api_key") apiKey: String
    ): WatchProvidersResponse

    // Onde assistir (RF34): disponibilidade de um filme por país.
    @GET("movie/{id}/watch/providers")
    suspend fun buscarProvedoresFilme(
        @Path("id") filmeId: Int,
        @Query("api_key") apiKey: String
    ): WatchProvidersResponse
    @GET("search/multi")
    suspend fun buscarMulti(
        @Query("api_key") apiKey: String,
        @Query("query") texto: String,
        @Query("language") idioma: String = "pt-BR"
    ): BuscaMultiResponse

    // append_to_response junta elenco (credits) e vídeos (videos) na mesma
    // resposta, evitando precisar de 3 chamadas separadas à API.
    @GET("tv/{id}")
    suspend fun buscarDetalhesSerie(
        @Path("id") serieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") idioma: String = "pt-BR",
        @Query("append_to_response") extras: String = "credits,videos"
    ): SerieDetalhe

    @GET("movie/{id}")
    suspend fun buscarDetalhesFilme(
        @Path("id") filmeId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") idioma: String = "pt-BR",
        @Query("append_to_response") extras: String = "credits,videos"
    ): MovieDetalhe

    @GET("tv/{id}/season/{numeroTemporada}")
    suspend fun buscarTemporada(
        @Path("id") serieId: Int,
        @Path("numeroTemporada") numeroTemporada: Int,
        @Query("api_key") apiKey: String,
        @Query("language") idioma: String = "pt-BR"
    ): TemporadaDetalhe

    @GET("trending/all/week")
    suspend fun buscarEmAlta(
        @Query("api_key") apiKey: String,
        @Query("language") idioma: String = "pt-BR"
    ): BuscaMultiResponse
}