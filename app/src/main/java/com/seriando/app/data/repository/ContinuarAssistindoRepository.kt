package com.seriando.app.data.repository

import com.seriando.app.data.local.Dependencias
import com.seriando.app.data.local.EpisodioDao
import com.seriando.app.model.ContinuarAssistindo
import com.seriando.app.model.ContinuarAssistindoRegras
import com.seriando.app.model.StatusSerie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * Monta a fileira "Continuar assistindo" da Home (RF83) usando SOMENTE dados do aparelho:
 * a lista e o progresso (Room) e os detalhes das séries já guardados (obrasCache).
 * Nenhuma chamada à TMDB nem ao Firestore é feita aqui, então não gasta requisições
 * (RNF19) e funciona sem internet.
 */
class ContinuarAssistindoRepository(
    private val lista: MinhaListaRepository = MinhaListaRepository(),
    private val episodios: EpisodioDao = Dependencias.episodioDao,
    private val obras: ObraCacheRepository = ObraCacheRepository()
) {

    /**
     * Séries "Assistindo" com progresso e próximo episódio, da mais recentemente assistida
     * para a mais antiga. O Flow emite de novo quando a lista muda ou quando qualquer
     * episódio é marcado ou desmarcado.
     */
    fun observar(): Flow<List<ContinuarAssistindo>> =
        // A contagem total de episódios serve só como "gatilho": mudou, recalcula tudo
        combine(lista.observarSeries(), episodios.observarTotalAssistidos()) { series, _ -> series }
            .map { series ->
                series
                    // Só séries (filme não tem episódio) com status "Assistindo"
                    .filter { it.tipoMidia == "tv" && it.status == StatusSerie.ASSISTINDO.name }
                    .map { serie ->
                        val vistos = episodios.listarDaSerie(serie.id)
                        // Detalhes guardados (pode ser nulo se a série nunca foi aberta/baixada)
                        val detalhe = obras.detalheSerieGuardado(serie.id)
                        val item = ContinuarAssistindo(
                            serie = serie,
                            assistidos = vistos.size,
                            total = detalhe?.numeroEpisodios ?: 0,
                            proximo = detalhe?.let {
                                ContinuarAssistindoRegras.proximoEpisodio(
                                    vistos.map { v -> v.temporada to v.numero },
                                    it.temporadas
                                )
                            }
                        )
                        // Para ordenar: instante do último episódio marcado (ou da própria série)
                        item to (vistos.maxOfOrNull { it.atualizadoEm } ?: serie.atualizadoEm)
                    }
                    .sortedByDescending { it.second }
                    .map { it.first }
                    .take(ContinuarAssistindoRegras.LIMITE_DA_FILEIRA)
            }
}
