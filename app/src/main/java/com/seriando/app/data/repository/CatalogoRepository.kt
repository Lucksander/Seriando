package com.seriando.app.data.repository

import com.seriando.app.BuildConfig
import com.seriando.app.data.cache.CatalogoCache
import com.seriando.app.data.remote.RetrofitInstance
import com.seriando.app.model.Categoria
import com.seriando.app.model.CatalogoDescoberta
import com.seriando.app.model.Genero
import com.seriando.app.model.ProvedorStreaming
import com.seriando.app.model.ResultadoBusca
import com.seriando.app.model.TipoDescoberta
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
 * Busca os títulos de cada categoria da Home na TMDB (RF44, RF45, RF46)
 * e os resultados da aba Descobrir (RF63, RF64).
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

    // ------------------------------------------------------------------
    // Aba Descobrir (RF63, RF64)
    // ------------------------------------------------------------------

    /**
     * Gêneros do tipo escolhido, em ordem alfabética (terceiro menu).
     * Os ids de filme e de série diferem na TMDB, por isso a lista é por tipo.
     * Guardada em memória (RNF13): muda raramente e as telas recriam o
     * repositório, então o cache fica no companion object.
     */
    suspend fun buscarGeneros(tipo: TipoDescoberta): List<Genero> {
        generosCache[tipo]?.let { return it }

        val resposta = when (tipo) {
            TipoDescoberta.FILMES -> RetrofitInstance.api.listarGenerosFilmes(BuildConfig.TMDB_API_KEY)
            TipoDescoberta.SERIES -> RetrofitInstance.api.listarGenerosSeries(BuildConfig.TMDB_API_KEY)
        }
        val generos = resposta.generos.sortedBy { it.nome }
        generosCache[tipo] = generos
        return generos
    }

    /**
     * Serviços de streaming do Brasil para o segundo menu: só os de maior
     * destaque (menor display_priority), para o menu não virar uma lista enorme.
     * Também guardada em memória (RNF13).
     */
    suspend fun buscarProvedores(tipo: TipoDescoberta): List<ProvedorStreaming> {
        provedoresCache[tipo]?.let { return it }

        val resposta = when (tipo) {
            TipoDescoberta.FILMES -> RetrofitInstance.api.listarProvedoresFilmes(BuildConfig.TMDB_API_KEY)
            TipoDescoberta.SERIES -> RetrofitInstance.api.listarProvedoresSeries(BuildConfig.TMDB_API_KEY)
        }
        val provedores = resposta.resultados
            .filter { it.id !in provedoresIgnorados } // tira lojas e agregadores
            .sortedBy { it.prioridade }
            .take(maximoProvedores)
        provedoresCache[tipo] = provedores
        return provedores
    }

    /**
     * Uma página da grade da aba Descobrir, sempre pela rede.
     *
     * @param tipo      Filmes ou Séries (primeiro menu).
     * @param catalogo  Popular, Novos ou um streaming (segundo menu).
     * @param generoId  id do gênero escolhido, ou null para "Todos" (terceiro menu).
     * @param pagina    número da página, começando em 1.
     */
    suspend fun descobrir(
        tipo: TipoDescoberta,
        catalogo: CatalogoDescoberta,
        generoId: Int?,
        pagina: Int
    ): PaginaCatalogo {
        val ehNovos = catalogo is CatalogoDescoberta.Novos
        val ehFilme = tipo == TipoDescoberta.FILMES

        // Novos ordena pela data de lançamento; os demais, por popularidade.
        // O nome do campo de data muda entre filmes e séries na TMDB.
        val ordenacao = when {
            !ehNovos -> "popularity.desc"
            ehFilme -> "primary_release_date.desc"
            else -> "first_air_date.desc"
        }

        // Em "Novos": nada de obras ainda não lançadas (data máxima = hoje) e
        // descarta as que não têm votos, para não encher a grade de títulos obscuros.
        val dataMaxima = if (ehNovos) hoje() else null
        val minVotos = if (ehNovos) votosMinimosNovos else null

        // Streaming (RF64): só quando o catálogo é um serviço. Região fixa BR (RN09)
        // e só "flatrate" (incluído na assinatura).
        val provedor = (catalogo as? CatalogoDescoberta.Streaming)?.provedor
        val provedorId = provedor?.id?.toString()
        val regiao = if (provedor != null) "BR" else null
        val monetizacao = if (provedor != null) "flatrate" else null

        val chave = BuildConfig.TMDB_API_KEY
        val resposta = if (ehFilme) {
            RetrofitInstance.api.descobrirFilmesComFiltros(
                apiKey = chave,
                generos = generoId?.toString(),
                provedores = provedorId,
                regiao = regiao,
                tiposMonetizacao = monetizacao,
                ordenacao = ordenacao,
                dataMaxima = dataMaxima,
                minVotos = minVotos,
                pagina = pagina
            )
        } else {
            RetrofitInstance.api.descobrirSeriesComFiltros(
                apiKey = chave,
                generos = generoId?.toString(),
                provedores = provedorId,
                regiao = regiao,
                tiposMonetizacao = monetizacao,
                ordenacao = ordenacao,
                dataMaxima = dataMaxima,
                minVotos = minVotos,
                pagina = pagina
            )
        }

        // discover não devolve media_type: definimos o tipo manualmente e
        // descartamos itens sem capa, como nas categorias da Home.
        val itens = resposta.results
            .map { it.copy(tipoMidia = tipo.valorTmdb) }
            .filter { it.caminhoCapa != null }
        return PaginaCatalogo(itens, minOf(resposta.totalPaginas, limitePaginas))
    }

    // Data de hoje no formato yyyy-MM-dd, exigido pela TMDB.
    // Usa SimpleDateFormat (e não java.time) porque o minSdk do app é 24.
    private fun hoje(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private companion object {
        // Quantos serviços de streaming aparecem no menu Catálogo
        const val maximoProvedores = 15

        // Ids que a TMDB lista como provedor, mas que não são catálogo de assinatura
        // útil no Brasil: Apple TV Store (loja), JustWatch TV Free (agregador) e Sun Nxt (Índia)
        val provedoresIgnorados = setOf(2, 2285, 309)

        // Mínimo de votos para uma obra aparecer em "Novos"
        const val votosMinimosNovos = 10

        // Caches em memória (não persistidos) das listas de gêneros e provedores, por tipo
        val generosCache = mutableMapOf<TipoDescoberta, List<Genero>>()
        val provedoresCache = mutableMapOf<TipoDescoberta, List<ProvedorStreaming>>()
    }
}