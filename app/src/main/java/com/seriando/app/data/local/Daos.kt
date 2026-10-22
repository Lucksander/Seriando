package com.seriando.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/*
 * Acesso às tabelas. Cada DAO é uma INTERFACE: o Room escreve a implementação, e os
 * repositórios dependem só destas interfaces (RNF11), nunca do banco em si.
 * As funções que devolvem Flow avisam a tela sozinhas quando os dados mudam (RN21);
 * as "suspend" rodam fora da thread principal.
 *
 * Sobre marcarSincronizado: só limpa a marca de pendente se o atualizadoEm ainda for o mesmo que
 * foi enviado. Se o usuário alterou o item de novo durante o envio, o atualizadoEm mudou e ele
 * continua pendente, e a alteração mais nova não se perde.
 */

@Dao
interface ListaDao {

    /** Itens da lista que a tela mostra, os mais recentes primeiro. Exclui os já removidos. */
    @Query("SELECT * FROM lista WHERE removido = 0 ORDER BY dataAdicao DESC")
    fun observarItens(): Flow<List<ListaEntity>>

    /** Um item pelo id, inclusive se estiver marcado como removido (quem chama confere). */
    @Query("SELECT * FROM lista WHERE id = :id")
    suspend fun obter(id: Int): ListaEntity?

    /** Insere ou substitui um item (a chave é o id). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvar(item: ListaEntity)

    /** Insere ou substitui vários itens de uma vez (usado ao baixar a lista da nuvem). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvarVarios(itens: List<ListaEntity>)

    /** Muda só o status de um item e o marca como pendente de envio. */
    @Query("UPDATE lista SET status = :status, atualizadoEm = :agora, pendenteSincronizacao = 1 WHERE id = :id")
    suspend fun atualizarStatus(id: Int, status: String, agora: Long)

    /** Remove o item da tela na hora, mas deixa a lápide para a exclusão ser enviada à nuvem. */
    @Query("UPDATE lista SET removido = 1, pendenteSincronizacao = 1, atualizadoEm = :agora WHERE id = :id")
    suspend fun marcarRemovido(id: Int, agora: Long)

    /** Apaga a linha de vez (depois que a nuvem confirmou a exclusão, ou ao limpar a lista). */
    @Query("DELETE FROM lista WHERE id = :id")
    suspend fun apagar(id: Int)

    /** Itens com alteração ainda não enviada, inclusive os removidos (lápides). */
    @Query("SELECT * FROM lista WHERE pendenteSincronizacao = 1")
    suspend fun pendentes(): List<ListaEntity>

    /** Quantidade de itens pendentes, para o aviso de alterações não sincronizadas (RF62). */
    @Query("SELECT COUNT(*) FROM lista WHERE pendenteSincronizacao = 1")
    fun observarQuantidadePendentes(): Flow<Int>

    /** Limpa a marca de pendente de um item, se ele não mudou desde o envio. */
    @Query("UPDATE lista SET pendenteSincronizacao = 0 WHERE id = :id AND atualizadoEm = :atualizadoEm")
    suspend fun marcarSincronizado(id: Int, atualizadoEm: Long)

    /** Apaga a lápide depois da exclusão confirmada na nuvem, se o item não mudou desde o envio. */
    @Query("DELETE FROM lista WHERE id = :id AND removido = 1 AND atualizadoEm = :atualizadoEm")
    suspend fun apagarRemovidoSincronizado(id: Int, atualizadoEm: Long)

    /** Total de itens na tabela (também usado no teste de fumaça ao abrir o app). */
    @Query("SELECT COUNT(*) FROM lista")
    suspend fun contar(): Int
}

@Dao
interface EpisodioDao {

    /** Episódios assistidos de uma série, atualizando a tela sozinho. Exclui os desmarcados. */
    @Query("SELECT * FROM episodiosAssistidos WHERE serieId = :serieId AND removido = 0")
    fun observarDaSerie(serieId: Int): Flow<List<EpisodioAssistidoEntity>>

    /** O mesmo, em leitura única. */
    @Query("SELECT * FROM episodiosAssistidos WHERE serieId = :serieId AND removido = 0")
    suspend fun listarDaSerie(serieId: Int): List<EpisodioAssistidoEntity>

    /** Marca um episódio como assistido (ou o reativa, se estava desmarcado). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvar(episodio: EpisodioAssistidoEntity)

    /** Marca vários episódios de uma vez (temporada inteira, ou download da nuvem). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvarVarios(episodios: List<EpisodioAssistidoEntity>)

    /** Desmarca um episódio, deixando a lápide para a exclusão ir à nuvem. */
    @Query(
        "UPDATE episodiosAssistidos SET removido = 1, pendenteSincronizacao = 1, atualizadoEm = :agora " +
                "WHERE serieId = :serieId AND temporada = :temporada AND numero = :numero"
    )
    suspend fun marcarRemovido(serieId: Int, temporada: Int, numero: Int, agora: Long)

    /** Apaga todo o progresso de uma série (ao removê-la da lista, RN20). */
    @Query("DELETE FROM episodiosAssistidos WHERE serieId = :serieId")
    suspend fun apagarTodosDaSerie(serieId: Int)

    /** Episódios com alteração ainda não enviada, inclusive as lápides. */
    @Query("SELECT * FROM episodiosAssistidos WHERE pendenteSincronizacao = 1")
    suspend fun pendentes(): List<EpisodioAssistidoEntity>

    /** Quantidade de episódios pendentes, para o aviso do RF62. */
    @Query("SELECT COUNT(*) FROM episodiosAssistidos WHERE pendenteSincronizacao = 1")
    fun observarQuantidadePendentes(): Flow<Int>

    /** Limpa a marca de pendente de um episódio, se ele não mudou desde o envio. */
    @Query(
        "UPDATE episodiosAssistidos SET pendenteSincronizacao = 0 " +
                "WHERE serieId = :serieId AND temporada = :temporada AND numero = :numero AND atualizadoEm = :atualizadoEm"
    )
    suspend fun marcarSincronizado(serieId: Int, temporada: Int, numero: Int, atualizadoEm: Long)

    /** Apaga a lápide depois da exclusão confirmada na nuvem, se o episódio não mudou desde o envio. */
    @Query(
        "DELETE FROM episodiosAssistidos WHERE serieId = :serieId AND temporada = :temporada " +
                "AND numero = :numero AND removido = 1 AND atualizadoEm = :atualizadoEm"
    )
    suspend fun apagarRemovidoSincronizado(serieId: Int, temporada: Int, numero: Int, atualizadoEm: Long)


    /** Um episódio pela chave composta, inclusive se estiver marcado como removido. */
    @Query("SELECT * FROM episodiosAssistidos WHERE serieId = :serieId AND temporada = :temporada AND numero = :numero")
    suspend fun obter(serieId: Int, temporada: Int, numero: Int): EpisodioAssistidoEntity?

    /** Apaga um episódio de vez do banco local. */
    @Query("DELETE FROM episodiosAssistidos WHERE serieId = :serieId AND temporada = :temporada AND numero = :numero")
    suspend fun apagar(serieId: Int, temporada: Int, numero: Int)

    /**
     * Desmarca todos os episódios ativos de uma série (ao removê-la da lista, RN20),
     * deixando lápides para a exclusão ir à nuvem.
     */
    @Query(
        "UPDATE episodiosAssistidos SET removido = 1, pendenteSincronizacao = 1, atualizadoEm = :agora " +
                "WHERE serieId = :serieId AND removido = 0"
    )
    suspend fun marcarTodosRemovidosDaSerie(serieId: Int, agora: Long)
}

@Dao
interface PerfilDao {

    /** Perfil do usuário, atualizando a tela sozinho (null enquanto ainda não existe). */
    @Query("SELECT * FROM perfil WHERE uid = :uid")
    fun observar(uid: String): Flow<PerfilEntity?>

    /** O mesmo, em leitura única. */
    @Query("SELECT * FROM perfil WHERE uid = :uid")
    suspend fun obter(uid: String): PerfilEntity?

    /** Cria ou substitui o perfil (há uma linha por usuário). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvar(perfil: PerfilEntity)

    /** Perfis com alteração ainda não enviada. */
    @Query("SELECT * FROM perfil WHERE pendenteSincronizacao = 1")
    suspend fun pendentes(): List<PerfilEntity>

    /** Quantidade de perfis pendentes (0 ou 1), para o aviso do RF62. */
    @Query("SELECT COUNT(*) FROM perfil WHERE pendenteSincronizacao = 1")
    fun observarQuantidadePendentes(): Flow<Int>

    /** Limpa a marca de pendente do perfil, se ele não mudou desde o envio. */
    @Query("UPDATE perfil SET pendenteSincronizacao = 0 WHERE uid = :uid AND atualizadoEm = :atualizadoEm")
    suspend fun marcarSincronizado(uid: String, atualizadoEm: Long)

    /** Quantos perfis de OUTROS usuários há no banco local (deveria ser zero, RN25). */
    @Query("SELECT COUNT(*) FROM perfil WHERE uid <> :uid")
    suspend fun contarDeOutrosUsuarios(uid: String): Int
}

@Dao
interface ObraCacheDao {

    /** Uma parte do cache de uma obra (parte -1 = detalhes; 0 ou mais = temporada). */
    @Query("SELECT * FROM obrasCache WHERE tipoMidia = :tipoMidia AND tmdbId = :tmdbId AND parte = :parte")
    suspend fun obter(tipoMidia: String, tmdbId: Int, parte: Int): ObraCacheEntity?

    /** Guarda ou atualiza uma parte do cache. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvar(cache: ObraCacheEntity)

    /** Apaga todo o cache de uma obra (por exemplo, ao removê-la da lista). */
    @Query("DELETE FROM obrasCache WHERE tipoMidia = :tipoMidia AND tmdbId = :tmdbId")
    suspend fun apagarDaObra(tipoMidia: String, tmdbId: Int)

    /**
     * Apaga o cache das obras que não estão (ou deixaram de estar) na lista. O cache é guardado
     * para toda obra aberta, mas só as da lista precisam ficar disponíveis offline.
     */
    @Query(
        "DELETE FROM obrasCache WHERE NOT EXISTS (" +
                "SELECT 1 FROM lista WHERE lista.id = obrasCache.tmdbId " +
                "AND lista.tipoMidia = obrasCache.tipoMidia AND lista.removido = 0)"
    )
    suspend fun apagarForaDaLista()
}