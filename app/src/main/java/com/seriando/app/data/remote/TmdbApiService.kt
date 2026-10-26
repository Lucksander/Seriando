package com.seriando.app.data.remote

import com.seriando.app.model.BuscaMultiResponse
import com.seriando.app.model.CreditosPessoaResponse
import com.seriando.app.model.GenerosResponse
import com.seriando.app.model.MovieDetalhe
import com.seriando.app.model.PessoaDetalhe
import com.seriando.app.model.ProvedoresResponse
import com.seriando.app.model.SerieDetalhe
import com.seriando.app.model.TemporadaDetalhe
import com.seriando.app.model.WatchProvidersResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import com.seriando.app.model.ContentRatingsResponse
import com.seriando.app.model.ReleaseDatesResponse

/**
 * Contrato Retrofit com a API da TMDB.
 * Cada função suspend é uma chamada HTTP; o Retrofit gera a implementação.
 */
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

    // Pesquisa unificada de filmes e séries (RF03).
    @GET("search/multi")
    suspend fun buscarMulti(
        @Query("api_key") apiKey: String,
        @Query("query") texto: String,
        @Query("language") idioma: String = "pt-BR"
    ): BuscaMultiResponse

    // append_to_response junta elenco (credits), vídeos (videos), classificação e
    // "onde assistir" (watch/providers) na mesma resposta, evitando chamadas separadas
    // à API. O "onde assistir" veio para cá para a tela de Detalhes fazer 1 chamada, não 2.
    @GET("tv/{id}")
    suspend fun buscarDetalhesSerie(
        @Path("id") serieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") idioma: String = "pt-BR",
        @Query("append_to_response") extras: String = "credits,videos,content_ratings,watch/providers"
    ): SerieDetalhe

    @GET("movie/{id}")
    suspend fun buscarDetalhesFilme(
        @Path("id") filmeId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") idioma: String = "pt-BR",
        @Query("append_to_response") extras: String = "credits,videos,release_dates,watch/providers"
    ): MovieDetalhe

    // Episódios de uma temporada (RF06).
    @GET("tv/{id}/season/{numeroTemporada}")
    suspend fun buscarTemporada(
        @Path("id") serieId: Int,
        @Path("numeroTemporada") numeroTemporada: Int,
        @Query("api_key") apiKey: String,
        @Query("language") idioma: String = "pt-BR"
    ): TemporadaDetalhe

    // Categoria "Em alta" (RF45). O parâmetro page permite paginar na tela
    // "Ver mais" (RF46); o valor padrão 1 preserva as chamadas já existentes.
    @GET("trending/all/week")
    suspend fun buscarEmAlta(
        @Query("api_key") apiKey: String,
        @Query("language") idioma: String = "pt-BR",
        @Query("page") pagina: Int = 1
    ): BuscaMultiResponse

    // Recomendações de um filme (RF81): obras que a TMDB considera parecidas.
    // A chave e o idioma seguem o padrão dos outros endpoints; só a 1ª página é usada.
    @GET("movie/{id}/recommendations")
    suspend fun recomendacoesFilme(
        @Path("id") filmeId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") idioma: String = "pt-BR",
        @Query("page") pagina: Int = 1
    ): BuscaMultiResponse

    // Recomendações de uma série (RF81).
    @GET("tv/{id}/recommendations")
    suspend fun recomendacoesSerie(
        @Path("id") serieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") idioma: String = "pt-BR",
        @Query("page") pagina: Int = 1
    ): BuscaMultiResponse

    // Filmes de um gênero (RF44), ordenados por popularidade.
    // with_genres recebe o id do gênero de FILMES da TMDB (ex.: 28 = Ação).
    // ATENÇÃO: discover/movie NÃO devolve o campo media_type; o repositório
    // precisa definir o tipo manualmente depois.
    @GET("discover/movie")
    suspend fun descobrirFilmes(
        @Query("api_key") apiKey: String,
        @Query("with_genres") generoId: Int,
        @Query("sort_by") ordenacao: String = "popularity.desc",
        @Query("language") idioma: String = "pt-BR",
        @Query("page") pagina: Int = 1
    ): BuscaMultiResponse

    // Séries de um gênero (RF44), ordenadas por popularidade.
    // with_genres recebe o id do gênero de SÉRIES (ex.: 10759 = Ação e Aventura).
    // Também não devolve media_type.
    @GET("discover/tv")
    suspend fun descobrirSeries(
        @Query("api_key") apiKey: String,
        @Query("with_genres") generoId: Int,
        @Query("sort_by") ordenacao: String = "popularity.desc",
        @Query("language") idioma: String = "pt-BR",
        @Query("page") pagina: Int = 1
    ): BuscaMultiResponse

    // Detalhes de uma pessoa (RF41): nome, foto, biografia, nascimento.
    @GET("person/{id}")
    suspend fun buscarPessoa(
        @Path("id") pessoaId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") idioma: String = "pt-BR"
    ): PessoaDetalhe

    // Filmografia (RF42): filmes e séries em que a pessoa atuou.
    @GET("person/{id}/combined_credits")
    suspend fun buscarCreditosPessoa(
        @Path("id") pessoaId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") idioma: String = "pt-BR"
    ): CreditosPessoaResponse

    // ------------------------------------------------------------------
    // Aba Descobrir (RF63, RF64): menus de Tipo, Catálogo e Gênero.
    // Parâmetros opcionais (nulos) NÃO são enviados pelo Retrofit, então a
    // mesma função serve para qualquer combinação de filtros.
    // ------------------------------------------------------------------

    // Lista de gêneros de FILMES (terceiro menu, quando o tipo é Filmes).
    @GET("genre/movie/list")
    suspend fun listarGenerosFilmes(
        @Query("api_key") apiKey: String,
        @Query("language") idioma: String = "pt-BR"
    ): GenerosResponse

    // Lista de gêneros de SÉRIES (terceiro menu, quando o tipo é Séries).
    @GET("genre/tv/list")
    suspend fun listarGenerosSeries(
        @Query("api_key") apiKey: String,
        @Query("language") idioma: String = "pt-BR"
    ): GenerosResponse

    // Serviços de streaming de FILMES disponíveis na região (segundo menu).
    @GET("watch/providers/movie")
    suspend fun listarProvedoresFilmes(
        @Query("api_key") apiKey: String,
        @Query("watch_region") regiao: String = "BR",
        @Query("language") idioma: String = "pt-BR"
    ): ProvedoresResponse

    // Serviços de streaming de SÉRIES disponíveis na região (segundo menu).
    @GET("watch/providers/tv")
    suspend fun listarProvedoresSeries(
        @Query("api_key") apiKey: String,
        @Query("watch_region") regiao: String = "BR",
        @Query("language") idioma: String = "pt-BR"
    ): ProvedoresResponse

    // Descobrir filmes com filtros combinados.
    //  - generos: id do gênero (ou null para todos).
    //  - provedores + regiao + tiposMonetizacao: filtro de streaming (RF64);
    //    "flatrate" = só quem está incluído na assinatura.
    //  - dataMaxima: só obras lançadas até essa data (yyyy-MM-dd), usada em "Novos".
    //  - minVotos: descarta obras sem votos, usada em "Novos".
    // Não devolve media_type; o repositório define o tipo depois.
    @GET("discover/movie")
    suspend fun descobrirFilmesComFiltros(
        @Query("api_key") apiKey: String,
        @Query("with_genres") generos: String? = null,
        @Query("with_watch_providers") provedores: String? = null,
        @Query("watch_region") regiao: String? = null,
        @Query("with_watch_monetization_types") tiposMonetizacao: String? = null,
        @Query("sort_by") ordenacao: String = "popularity.desc",
        @Query("primary_release_date.lte") dataMaxima: String? = null,
        @Query("vote_count.gte") minVotos: Int? = null,
        @Query("language") idioma: String = "pt-BR",
        @Query("page") pagina: Int = 1
    ): BuscaMultiResponse

    // Descobrir séries com filtros combinados (mesma ideia dos filmes; o campo
    // de data das séries na TMDB é first_air_date, não primary_release_date).
    @GET("discover/tv")
    suspend fun descobrirSeriesComFiltros(
        @Query("api_key") apiKey: String,
        @Query("with_genres") generos: String? = null,
        @Query("with_watch_providers") provedores: String? = null,
        @Query("watch_region") regiao: String? = null,
        @Query("with_watch_monetization_types") tiposMonetizacao: String? = null,
        @Query("sort_by") ordenacao: String = "popularity.desc",
        @Query("first_air_date.lte") dataMaxima: String? = null,
        @Query("vote_count.gte") minVotos: Int? = null,
        @Query("language") idioma: String = "pt-BR",
        @Query("page") pagina: Int = 1
    ): BuscaMultiResponse

    // Controle de idade (RF50): classificação de um filme em cada país. A região BR é
    // escolhida no app (ClassificacaoIndicativa.doFilme), não na chamada.
    @GET("movie/{id}/release_dates")
    suspend fun buscarClassificacaoFilme(
        @Path("id") filmeId: Int,
        @Query("api_key") apiKey: String
    ): ReleaseDatesResponse

    // Controle de idade (RF50): classificação de uma série em cada país.
    @GET("tv/{id}/content_ratings")
    suspend fun buscarClassificacaoSerie(
        @Path("id") serieId: Int,
        @Query("api_key") apiKey: String
    ): ContentRatingsResponse

}