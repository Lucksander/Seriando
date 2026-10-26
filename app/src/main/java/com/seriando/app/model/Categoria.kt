package com.seriando.app.model

/**
 * Representa uma categoria da Home (RF44, RF45).
 *
 * @param id             identificador usado na rota de navegação (categoria/{id}) e como chave de cache.
 * @param nome           texto exibido na interface, em português.
 * @param generoFilmeId  id do gênero na TMDB para filmes (discover/movie). Nulo em "Em alta".
 * @param generoSerieId  id do gênero na TMDB para séries (discover/tv).
 *                       Nulo quando o gênero só existe para filmes (Terror e Romance).
 * @param tipoEmAlta     "movie" ou "tv" nas categorias "Filmes em alta" e "Séries em alta", que não
 *                       usam gênero e sim o endpoint trending/{tipo}/week. Nulo nas demais.
 */
data class Categoria(
    val id: String,
    val nome: String,
    val generoFilmeId: Int? = null,
    val generoSerieId: Int? = null,
    val tipoEmAlta: String? = null
) {
    // Verdadeiro nas categorias de "em alta" (usa o trending em vez do discover por gênero)
    val emAlta: Boolean get() = tipoEmAlta != null
}

/**
 * Lista fixa de categorias, na ordem em que aparecem na Home.
 * Os ids de gênero das séries diferem dos de filmes na TMDB:
 *  - Ação: filme 28 / série 10759 (Ação e Aventura)
 *  - Ficção científica: filme 878 / série 10765 (Sci-Fi e Fantasia)
 *  - Suspense: filme 53 (Thriller) / série 9648 (Mistério)
 */
object Categorias {

    // Todas as categorias exibidas na Home, de cima para baixo
    val todas: List<Categoria> = listOf(
        // Duas fileiras de "em alta" separadas: uma só de séries e outra só de filmes (RF45)
        Categoria(id = "series_em_alta", nome = "Séries em alta", tipoEmAlta = "tv"),
        Categoria(id = "filmes_em_alta", nome = "Filmes em alta", tipoEmAlta = "movie"),
        Categoria(id = "acao", nome = "Ação", generoFilmeId = 28, generoSerieId = 10759),
        Categoria(id = "comedia", nome = "Comédia", generoFilmeId = 35, generoSerieId = 35),
        Categoria(id = "suspense", nome = "Suspense", generoFilmeId = 53, generoSerieId = 9648),
        Categoria(id = "drama", nome = "Drama", generoFilmeId = 18, generoSerieId = 18),
        Categoria(id = "ficcao", nome = "Ficção científica", generoFilmeId = 878, generoSerieId = 10765),
        // Terror e Romance não têm gênero equivalente em séries: só filmes
        Categoria(id = "terror", nome = "Terror", generoFilmeId = 27, generoSerieId = null),
        Categoria(id = "romance", nome = "Romance", generoFilmeId = 10749, generoSerieId = null),
        Categoria(id = "animacao", nome = "Animação", generoFilmeId = 16, generoSerieId = 16),
        Categoria(id = "crime", nome = "Crime", generoFilmeId = 80, generoSerieId = 80),
        Categoria(id = "documentario", nome = "Documentário", generoFilmeId = 99, generoSerieId = 99),
        Categoria(id = "familia", nome = "Família", generoFilmeId = 10751, generoSerieId = 10751)
    )

    /** Busca uma categoria pelo id da rota; devolve null se não existir. */
    fun porId(id: String): Categoria? = todas.firstOrNull { it.id == id }
}