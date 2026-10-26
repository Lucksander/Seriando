package com.seriando.app.data.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParseException
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
 * Cada busca olha primeiro a cópia no banco local (tabela obrasCache): se ela ainda está
 * dentro da validade (VALIDADE_*), é usada direto, SEM chamar a TMDB. Se venceu ou não existe,
 * tenta a rede e, se der certo, atualiza a cópia. Se a rede falhar, devolve a última cópia
 * guardada, mesmo vencida; sem cópia, repassa o erro.
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
        val detalhe = comCache(SERIE, id, PARTE_DETALHE, SerieDetalhe::class.java, VALIDADE_DETALHE) {
            RetrofitInstance.api.buscarDetalhesSerie(id, BuildConfig.TMDB_API_KEY)
                .also { guardarProvedoresDosDetalhes(SERIE, id, it.provedores) }
        }
        prebaixarTemporadasSeEstiverNaLista(id, detalhe)
        return detalhe
    }

    /**
     * Detalhes da série guardados no aparelho, de qualquer idade, SEM consultar a rede.
     * Usado pela fileira "Continuar assistindo" (RF83). Devolve null se nunca foram guardados
     * ou se a cópia estiver ilegível.
     */
    suspend fun detalheSerieGuardado(id: Int): SerieDetalhe? {
        val copia = cache.obter(SERIE, id, PARTE_DETALHE) ?: return null
        return try {
            gson.fromJson(copia.json, SerieDetalhe::class.java)
        } catch (e: JsonParseException) {
            null
        }
    }

    /** Detalhes de um filme. */
    suspend fun buscarDetalheFilme(id: Int): MovieDetalhe =
        comCache(FILME, id, PARTE_DETALHE, MovieDetalhe::class.java, VALIDADE_DETALHE) {
            RetrofitInstance.api.buscarDetalhesFilme(id, BuildConfig.TMDB_API_KEY)
                .also { guardarProvedoresDosDetalhes(FILME, id, it.provedores) }
        }

    /** Episódios de uma temporada de uma série. */
    suspend fun buscarEpisodios(serieId: Int, numeroTemporada: Int): List<EpisodioResumo> =
        comCache(SERIE, serieId, numeroTemporada, TemporadaDetalhe::class.java, VALIDADE_TEMPORADA) {
            RetrofitInstance.api.buscarTemporada(serieId, numeroTemporada, BuildConfig.TMDB_API_KEY)
        }.episodios

    /**
     * O "onde assistir" agora vem dentro dos detalhes (append_to_response). Esta função o guarda
     * também como a parte própria de provedores, para o modo offline e o download em segundo
     * plano continuarem funcionando como antes, sem uma chamada extra. É chamada só quando os
     * detalhes acabaram de vir da REDE (dentro do lambda de comCache), então a data da cópia
     * nunca é renovada com dados antigos.
     */
    private suspend fun guardarProvedoresDosDetalhes(tipoMidia: String, id: Int, provedores: WatchProvidersResponse?) {
        if (provedores == null) return
        cache.salvar(
            ObraCacheEntity(
                tipoMidia = tipoMidia,
                tmdbId = id,
                parte = PARTE_PROVEDORES,
                json = gson.toJson(provedores),
                atualizadoEm = System.currentTimeMillis()
            )
        )
    }

    /** Onde assistir (streaming), de um filme ou de uma série. */
    suspend fun buscarProvedores(tipoMidia: String, id: Int): WatchProvidersResponse =
        comCache(tipoMidia, id, PARTE_PROVEDORES, WatchProvidersResponse::class.java, VALIDADE_PROVEDORES) {
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
     * 1) Se há cópia DENTRO da validade, devolve ela e não chama a rede (poupa requisições).
     * 2) Senão, busca na rede e guarda a cópia nova.
     * 3) Se a rede falhar, devolve a cópia guardada (mesmo vencida), se existir.
     * O cancelamento da corrotina (a tela saiu) é repassado, nunca confundido com falha de rede.
     *
     * @param validadeMs por quanto tempo, em milissegundos, a cópia vale sem consultar a TMDB.
     */
    private suspend fun <T> comCache(
        tipoMidia: String,
        id: Int,
        parte: Int,
        classe: Class<T>,
        validadeMs: Long,
        buscarNaRede: suspend () -> T
    ): T {
        // Passo 1: cópia fresca? A idade precisa estar entre 0 e a validade; uma data no
        // futuro (relógio do aparelho mexido) não conta como fresca.
        val copia = cache.obter(tipoMidia, id, parte)
        if (copia != null) {
            val idade = System.currentTimeMillis() - copia.atualizadoEm
            if (idade in 0 until validadeMs) {
                try {
                    return gson.fromJson(copia.json, classe)
                } catch (e: JsonParseException) {
                    // Cópia ilegível: ignora e segue para a rede
                    Log.d(TAG, "Cópia ilegível de $tipoMidia $id (parte $parte), buscando de novo")
                }
            }
        }

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

        // Validade das cópias (por quanto tempo valem sem consultar a TMDB). Ajuste aqui se quiser
        // mais ou menos frescor: detalhes e temporadas mudam com novos episódios; o "onde
        // assistir" muda pouco.
        const val VALIDADE_DETALHE = 6L * 60 * 60 * 1000       // 6 horas
        const val VALIDADE_TEMPORADA = 6L * 60 * 60 * 1000     // 6 horas
        const val VALIDADE_PROVEDORES = 24L * 60 * 60 * 1000   // 24 horas

        // Partes guardadas de cada obra. Temporadas usam o próprio número (0 = especiais).
        const val PARTE_DETALHE = -1
        const val PARTE_PROVEDORES = -2

        // Escopo e trava compartilhados: o download em segundo plano não depende de nenhuma tela
        val escopo = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val baixando = AtomicBoolean(false)
    }
}