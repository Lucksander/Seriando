package com.seriando.app.data.repository

import com.seriando.app.data.local.Dependencias
import com.seriando.app.data.local.EpisodioDao
import com.seriando.app.data.local.ListaDao
import com.seriando.app.data.local.paraEntity
import com.seriando.app.data.local.paraSerieSalva
import com.seriando.app.data.sync.Sincronizador
import com.seriando.app.model.SerieSalva
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Lista pessoal do usuário (RF07, RF08), offline-first (RF33).
 *
 * As telas leem e gravam SEMPRE no banco local (RN21). Cada alteração grava primeiro no Room,
 * marcada como pendente (RN22), e pede ao Sincronizador que a envie ao Firestore em segundo
 * plano. Por isso estas funções funcionam sem internet e nunca esperam pela nuvem.
 *
 * O parâmetro uid das funções é mantido para as telas continuarem iguais: o banco local só
 * guarda os dados do usuário logado (RN25) e o uid é usado para a sincronização.
 *
 * @param local     DAO da lista (interface do Room, RNF11).
 * @param episodios DAO dos episódios, usado para apagar o progresso junto com o item (RN20).
 */
class MinhaListaRepository(
    private val local: ListaDao = Dependencias.listaDao,
    private val episodios: EpisodioDao = Dependencias.episodioDao
) {

    /** A lista, atualizada sozinha sempre que o banco local mudar (inclusive após sincronizar). */
    fun observarSeries(): Flow<List<SerieSalva>> =
        local.observarItens().map { itens -> itens.map { it.paraSerieSalva() } }

    /** A lista numa leitura única, do banco local. */
    suspend fun buscarSeries(uid: String): List<SerieSalva> =
        local.observarItens().first().map { it.paraSerieSalva() }

    /** Adiciona (ou readiciona) um item à lista, pendente de envio. */
    suspend fun adicionarSerie(uid: String, serie: SerieSalva) {
        local.salvar(serie.paraEntity(pendente = true, atualizadoEm = System.currentTimeMillis()))
        // Só envia: baixar a nuvem inteira a cada alteração geraria leituras demais
        Sincronizador.disparar(uid, baixarDaNuvem = false)
    }

    /**
     * Remove o item da lista (RN20). O item e todo o progresso de episódios dele somem da tela
     * na hora, numa única transação; as lápides ficam no banco até as exclusões irem à nuvem.
     * Os episódios também ficam como lápides (em vez de apagados) para a exclusão deles chegar
     * à nuvem mesmo que o item seja readicionado antes da sincronização.
     */
    suspend fun removerSerie(uid: String, serieId: Int) {
        val agora = System.currentTimeMillis()
        Dependencias.emTransacao {
            local.marcarRemovido(serieId, agora)
            episodios.marcarTodosRemovidosDaSerie(serieId, agora)
        }
        Sincronizador.disparar(uid, baixarDaNuvem = false)
    }

    /** true se o item está na lista (itens marcados como removidos não contam). */
    suspend fun contemSerie(uid: String, serieId: Int): Boolean =
        local.obter(serieId)?.removido == false

    /**
     * Muda só o status de um item já salvo. Recebe o nome do enum já pronto
     * (StatusSerie ou StatusFilme, resolvido pela tela).
     */
    suspend fun atualizarStatusTexto(uid: String, serieId: Int, nomeStatus: String) {
        local.atualizarStatus(serieId, nomeStatus, System.currentTimeMillis())
        Sincronizador.disparar(uid, baixarDaNuvem = false)
    }
}