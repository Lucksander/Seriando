package com.seriando.app.data.repository

import com.seriando.app.BuildConfig
import com.seriando.app.data.cache.CatalogoCache
import com.seriando.app.data.remote.RetrofitInstance
import com.seriando.app.model.Categoria
import com.seriando.app.model.ResultadoBusca
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Resultado de uma página de categoria: os itens e o total de páginas
 * disponíveis, usado pela tela "Ver mais" para saber quando esconder
 * o botão "Carregar mais" (RF46).
 */
data class PaginaCatalogo(
    val itens: List<ResultadoBusca>,
    val totalPaginas: Int
)

/**
 * Busca os títulos de cada categoria da Home na TMDB (RF44, RF45, RF46).
 * As funções são suspend e NÃO tratam erros: quem chama (a tela) faz
 * try/catch, registra no Log e avisa o usuário com Toast.
 */
class CatalogoRepository {

    // A TMDB limita a paginação do discover a 500 páginas.
    private val limitePaginas = 500

    /**
     * Itens da 1ª página de uma categoria, usados nas fileiras da Home.
     * Usa o CatalogoCache: se a categoria já foi carregada, devolve na hora,
     * sem nova chamada de rede. Caso contrário busca, guarda no cache e devolve.
     */
    suspend fun buscarPrimeirosItens(categoria: Categoria): List<ResultadoBusca> {
        // Cache primeiro: evita refazer chamadas ao voltar de Detalhes
        CatalogoCache.obter(categoria.id)?.let { return it }

        val itens = buscarPagina(categoria, pagina = 1).itens
        CatalogoCache.guardar(categoria.id, itens)
        return itens
    }

    /**
     * Busca uma página qualquer da categoria, sempre pela rede.
     * Usada pela tela "Ver mais" (grade completa + "Carregar mais").
     *
     * @param categoria categoria desejada.
     * @param pagina    número da página, começando em 1.
     */
    suspend fun buscarPagina(categoria: Categoria, pagina: Int): PaginaCatalogo {
        return if (categoria.emAlta) buscarEmAlta(pagina) else buscarPorGenero(categoria, pagina)
    }

    // "Em alta" (RF45): o trending já devolve media_type em cada item.
    private suspend fun buscarEmAlta(pagina: Int): PaginaCatalogo {
        val resposta = RetrofitInstance.api.buscarEmAlta(
            apiKey = BuildConfig.TMDB_API_KEY,
            pagina = pagina
        )
        val itens = resposta.results
            // Remove pessoas (e qualquer tipo desconhecido) e itens sem capa
            .filter { (it.ehFilme || it.ehSerie) && it.caminhoCapa != null }
        return PaginaCatalogo(itens, minOf(resposta.totalPaginas, limitePaginas))
    }

    // Categorias por gênero (RF44): filmes e séries chamados em paralelo.
    private suspend fun buscarPorGenero(categoria: Categoria, pagina: Int): PaginaCatalogo =
        // coroutineScope garante que as duas chamadas terminam (ou falham) juntas
        coroutineScope {
            val filmesAsync = categoria.generoFilmeId?.let { generoId ->
                async {
                    RetrofitInstance.api.descobrirFilmes(
                        apiKey = BuildConfig.TMDB_API_KEY,
                        generoId = generoId,
                        pagina = pagina
                    )
                }
            }
            // Séries só entram se a categoria tiver gênero equivalente (Terror e Romance não têm)
            val seriesAsync = categoria.generoSerieId?.let { generoId ->
                async {
                    RetrofitInstance.api.descobrirSeries(
                        apiKey = BuildConfig.TMDB_API_KEY,
                        generoId = generoId,
                        pagina = pagina
                    )
                }
            }

            val respostaFilmes = filmesAsync?.await()
            val respostaSeries = seriesAsync?.await()

            // discover não devolve media_type: definimos o tipo manualmente com copy()
            val filmes = respostaFilmes?.results.orEmpty()
                .map { it.copy(tipoMidia = "movie") }
                .filter { it.caminhoCapa != null }
            val series = respostaSeries?.results.orEmpty()
                .map { it.copy(tipoMidia = "tv") }
                .filter { it.caminhoCapa != null }

            // Total de páginas: o menor entre os dois lados, para nunca pedir
            // uma página que um deles não tem. Se só um existe, vale o dele.
            val totais = listOfNotNull(respostaFilmes?.totalPaginas, respostaSeries?.totalPaginas)
            val total = (totais.minOrNull() ?: 0).coerceAtMost(limitePaginas)

            PaginaCatalogo(intercalar(filmes, series), total)
        }

    /**
     * Mistura as duas listas alternando filme, série, filme, série...
     * Se uma acabar antes, o restante da outra segue em sequência.
     */
    private fun intercalar(
        filmes: List<ResultadoBusca>,
        series: List<ResultadoBusca>
    ): List<ResultadoBusca> {
        val resultado = mutableListOf<ResultadoBusca>()
        for (i in 0 until maxOf(filmes.size, series.size)) {
            filmes.getOrNull(i)?.let { resultado.add(it) }
            series.getOrNull(i)?.let { resultado.add(it) }
        }
        return resultado
    }
}