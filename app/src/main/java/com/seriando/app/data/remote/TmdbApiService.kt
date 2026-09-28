package com.seriando.app.data.remote

import com.seriando.app.model.TmdbSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbApiService {
    @GET("search/tv")
    suspend fun buscarSeries(
        @Query("api_key") apiKey: String,
        @Query("query") nomeSerie: String,
        @Query("language") idioma: String = "pt-BR"
    ): TmdbSearchResponse
}