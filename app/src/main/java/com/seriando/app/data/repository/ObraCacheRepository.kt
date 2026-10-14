package com.seriando.app.data.repository

import android.util.Log
import com.google.gson.Gson
import com.seriando.app.BuildConfig
import com.seriando.app.data.local.Dependencias
import com.seriando.app.data.local.ListaDao
import com.seriando.app.data.local.ObraCacheDao
import com.seriando.app.data.local.ObraCacheEntity
import com.seriando.app.data.remote.RetrofitInstance
import com.seriando.app.model.EpisodioResumo
import com.seriando.app.model.MovieDetalhe
import com.seriando.app.model.SerieDetalhe
import com.seriando.app.model.TemporadaDetalhe
import com.seriando.app.model.WatchProvidersResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Dados da TMDB das obras, com cópia no aparelho para funcionar sem internet (RF33, RNF13).
 *
 * Cada busca tenta a rede primeiro e, se der certo, atualiza a cópia no banco local (tabela
 * obrasCache). Se a rede falhar, devolve a última cópia guardada; sem cópia, repassa o erro.
 * A cópia é o texto JSON da resposta, feito com o mesmo conversor (Gson) que o Retrofit usa.
 *
 * Toda obra aberta é guardada, mas o Sincronizador apaga, ao abrir o app, as que não estão na
 * lista. As obras da lista são baixadas em segundo plano (detalhes, "onde assistir" e todas as
 * temporadas), para ficarem disponíveis offline mesmo sem o usuário abrir cada uma.
 *
 * As funções NÃO tratam o erro final: quem chama (a tela) faz try/catch.
 *
 * @param cache DAO do cache (interface do Room, RNF11).
 * @param lista DAO da lista, para saber quais obras estão salvas.
 */
class ObraCacheRepository(
    private val cache: ObraCacheDao = Dependencias.obraCacheDao,
    private val lista: ListaDao = Dependencias.listaDao
) {

    private val gson = Gson()

    /** Detalhes de uma série (sinopse, elenco, vídeos e lista de temporadas). */
    suspend fun buscarDetalheSerie(id: Int): SerieDetalhe {
        val detalhe = comCache(SERIE, id, PARTE_DETALHE, SerieDetalhe::class.java) {
            RetrofitInstance.api.buscarDetalhesSerie(id, BuildConfig.TMDB_API_KEY)
        }
        prebaixarTemporadasSeEstiverNaLista(id, detalhe)
        return detalhe
    }

    /** Detalhes de um filme. */
    suspend fun buscarDetalheFilme(id: Int): MovieDetalhe =
        comCache(FILME, id, PARTE_DETALHE, MovieDetalhe::class.java) {
            RetrofitInstance.api.buscarDetalhesFilme(id, BuildConfig.TMDB_API_KEY)
        }

    /** Episódios de uma temporada de uma série. */
    suspend fun buscarEpisodios(serieId: Int, numeroTemporada: Int): List<EpisodioResumo> =
        comCache(SERIE, serieId, numeroTemporada, TemporadaDetalhe::class.java) {
            RetrofitInstance.api.buscarTemporada(serieId, numeroTemporada, BuildConfig.TMDB_API_KEY)
        }.episodios

    /** Onde assistir (streaming), de um filme ou de uma série. */
    suspend fun buscarProvedores(tipoMidia: String, id: Int): WatchProvidersResponse =
        comCache(tipoMidia, id, PARTE_PROVEDORES, WatchProvidersResponse::class.java) {
            if (tipoMidia == FILME) {
                RetrofitInstance.api.buscarProvedoresFilme(id, BuildConfig.TMDB_API_KEY)
            } else {
                RetrofitInstance.api.buscarProvedoresSerie(id, BuildConfig.TMDB_API_KEY)
            }
        }

    /**
     * Baixa em segundo plano os dados das obras da lista que ainda não têm cópia no aparelho.
     * Só uma rodada acontece por vez. Chamada pelo Sincronizador depois de baixar a lista.
     * Sem internet, as tentativas falham em silêncio e são refeitas na próxima sincronização.
     */
    fun baixarObrasDaListaEmSegundoPlano() {
        if (!baixando.compareAndSet(false, true)) return

        escopo.launch {
            try {
                for (item in lista.observarItens().first()) {
                    if (cache.obter(item.tipoMidia, item.id, PARTE_DETALHE) != null) continue
                    try {
                        if (item.tipoMidia == SERIE) buscarDetalheSerie(item.id) else buscarDetalheFilme(item.id)
                        if (cache.obter(item.tipoMidia, item.id, PARTE_PROVEDORES) == null) {
                            buscarProvedores(item.tipoMidia, item.id)
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.d(TAG, "Sem cópia de ${item.nome} por enquanto: ${e.message}")
                    }
                }
            } finally {
                baixando.set(false)
            }
        }
    }

    /**
     * Se a série está na lista, baixa em segundo plano as temporadas que ainda não têm cópia,
     * para o usuário poder abrir qualquer temporada sem internet.
     */
    private fun prebaixarTemporadasSeEstiverNaLista(id: Int, serie: SerieDetalhe) {
        escopo.launch {
            try {
                val item = lista.obter(id)
                if (item == null || item.removido) return@launch

                for (temporada in serie.temporadas) {
                    if (cache.obter(SERIE, id, temporada.numero) == null) {
                        buscarEpisodios(id, temporada.numero)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.d(TAG, "Temporadas não baixadas agora: ${e.message}")
            }
        }
    }

    /**
     * Busca na rede e guarda a cópia; se a rede falhar, devolve a cópia guardada (se existir).
     * O cancelamento da corrotina (a tela saiu) é repassado, nunca confundido com falha de rede.
     */
    private suspend fun <T> comCache(
        tipoMidia: String,
        id: Int,
        parte: Int,
        classe: Class<T>,
        buscarNaRede: suspend () -> T
    ): T {
        try {
            val resposta = buscarNaRede()
            cache.salvar(
                ObraCacheEntity(
                    tipoMidia = tipoMidia,
                    tmdbId = id,
                    parte = parte,
                    json = gson.toJson(resposta),
                    atualizadoEm = System.currentTimeMillis()
                )
            )
            return resposta
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Sem rede (ou TMDB fora do ar): usa a cópia guardada, se houver; sem cópia, repassa o erro
            val guardado = cache.obter(tipoMidia, id, parte) ?: throw e
            Log.d(TAG, "Usando a cópia guardada de $tipoMidia $id (parte $parte)")
            return gson.fromJson(guardado.json, classe)
        }
    }

    private companion object {
        const val TAG = "ObraCache"
        const val SERIE = "tv"
        const val FILME = "movie"

        // Partes guardadas de cada obra. Temporadas usam o próprio número (0 = especiais).
        const val PARTE_DETALHE = -1
        const val PARTE_PROVEDORES = -2

        // Escopo e trava compartilhados: o download em segundo plano não depende de nenhuma tela
        val escopo = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val baixando = AtomicBoolean(false)
    }
}