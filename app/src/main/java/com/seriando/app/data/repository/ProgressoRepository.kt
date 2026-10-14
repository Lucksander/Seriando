package com.seriando.app.data.repository

import com.seriando.app.data.local.Dependencias
import com.seriando.app.data.local.EpisodioAssistidoEntity
import com.seriando.app.data.local.EpisodioDao
import com.seriando.app.data.sync.Sincronizador
import com.seriando.app.model.EpisodioResumo

/**
 * Progresso de episódios (RF09, RF10), offline-first (RF33).
 *
 * Tudo é lido e gravado no banco local (RN21); cada alteração fica marcada como pendente
 * (RN22) e o Sincronizador a envia ao Firestore em segundo plano. Por isso estas funções
 * funcionam sem internet e nunca esperam pela nuvem.
 *
 * O parâmetro uid é mantido para as telas continuarem iguais: o banco local só guarda
 * os dados do usuário logado (RN25) e o uid é usado na sincronização.
 *
 * @param local DAO dos episódios (interface do Room, RNF11).
 */
class ProgressoRepository(
    private val local: EpisodioDao = Dependencias.episodioDao
) {

    /** Marca um episódio como assistido, pendente de envio. */
    suspend fun marcarAssistido(uid: String, serieId: Int, temporada: Int, numero: Int) {
        local.salvar(episodioPendente(serieId, temporada, numero, System.currentTimeMillis()))
        // Só envia: baixar a nuvem inteira a cada clique geraria leituras demais
        Sincronizador.disparar(uid, baixarDaNuvem = false)
    }

    /** Desmarca um episódio. Some da tela na hora; a lápide fica até a exclusão ir à nuvem. */
    suspend fun desmarcarAssistido(uid: String, serieId: Int, temporada: Int, numero: Int) {
        local.marcarRemovido(serieId, temporada, numero, System.currentTimeMillis())
        Sincronizador.disparar(uid, baixarDaNuvem = false)
    }

    /**
     * Episódios assistidos de uma série, como conjunto de chaves "temporada_numero"
     * (o formato que a tela de Detalhes já usa), lidos do banco local.
     */
    suspend fun buscarAssistidos(uid: String, serieId: Int): Set<String> =
        local.listarDaSerie(serieId).map { "${it.temporada}_${it.numero}" }.toSet()

    /**
     * Marca todos os episódios de uma temporada de uma vez, numa única gravação no banco local.
     */
    suspend fun marcarTemporadaComoAssistida(
        uid: String,
        serieId: Int,
        numeroTemporada: Int,
        episodios: List<EpisodioResumo>
    ) {
        val agora = System.currentTimeMillis()
        local.salvarVarios(episodios.map { episodioPendente(serieId, numeroTemporada, it.numero, agora) })
        Sincronizador.disparar(uid, baixarDaNuvem = false)
    }

    /** Monta a linha do banco local de um episódio assistido, ainda pendente de envio. */
    private fun episodioPendente(serieId: Int, temporada: Int, numero: Int, agora: Long) =
        EpisodioAssistidoEntity(
            serieId = serieId,
            temporada = temporada,
            numero = numero,
            atualizadoEm = agora,
            pendenteSincronizacao = true,
            removido = false
        )
}