package com.seriando.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/*
 * Tabelas do banco local (Room) do modo offline (RF33, RF62; seção 4.1 dos requisitos).
 * O banco guarda só os dados do usuário logado (RN25) e espelha o que está no Firestore.
 *
 * Campos de controle da sincronização, presentes nas tabelas que o usuário altera:
 *  - atualizadoEm: instante da última alteração local, em milissegundos. Resolve conflitos
 *    com a nuvem pela gravação mais recente (RN23). Vem do relógio do celular (limitação conhecida).
 *  - pendenteSincronizacao: true enquanto a alteração ainda não foi enviada ao Firestore (RN22).
 *  - removido: "lápide". Um item removido sem conexão não some do banco na hora: fica marcado
 *    como removido e pendente até a exclusão ser enviada à nuvem, só então a linha é apagada.
 */

/** Lista pessoal (RF07, RF08). Espelha usuarios/{uid}/series/{id}; a chave é o id da TMDB. */
@Entity(tableName = "lista")
data class ListaEntity(
    @PrimaryKey val id: Int,
    val nome: String,
    val sinopse: String,
    val caminhoCapa: String?,
    val tipoMidia: String,          // "tv" ou "movie"
    val status: String,             // nome do enum StatusSerie ou StatusFilme
    val dataAdicao: Long,
    val atualizadoEm: Long,
    val pendenteSincronizacao: Boolean,
    val removido: Boolean = false
)

/** Episódios assistidos (RF09). Espelha usuarios/{uid}/series/{id}/episodiosAssistidos/{temporada_numero}. */
@Entity(
    tableName = "episodiosAssistidos",
    primaryKeys = ["serieId", "temporada", "numero"]
)
data class EpisodioAssistidoEntity(
    val serieId: Int,
    val temporada: Int,
    val numero: Int,
    val atualizadoEm: Long,
    val pendenteSincronizacao: Boolean,
    val removido: Boolean = false   // episódio desmarcado sem conexão, aguardando envio
)

/**
 * Perfil do próprio usuário (RF12 a RF15). Uma única linha, que junta perfisPublicos/{uid}
 * (apelido e avatar) e usuarios/{uid} (data de nascimento). O e-mail não é guardado aqui: o
 * Firebase Auth já o mantém no aparelho (coleta mínima, RNF08).
 */
@Entity(tableName = "perfil")
data class PerfilEntity(
    @PrimaryKey val uid: String,
    val apelido: String,
    val avatarId: String,
    // Data de nascimento (RF59) no formato "AAAA-MM-DD". Valores antigos em texto livre
    // (ex: "15/03") continuam aqui até o usuário informar a data completa
    val aniversario: String?,
    val atualizadoEm: Long,
    val pendenteSincronizacao: Boolean,
    // true depois que o usuário usou a única correção da data (RN17). Espelha o campo
    // aniversarioAlterado de usuarios/{uid}. O valor padrão mantém o código antigo compilando
    val aniversarioCorrigido: Boolean = false
)

/**
 * Respostas da TMDB das obras da lista, guardadas como texto JSON para consulta sem internet.
 * Uma linha por parte da obra: parte = -1 guarda os detalhes (sinopse, elenco, temporadas) e
 * parte >= 0 guarda os episódios de uma temporada (0 = especiais). É só cache: nunca
 * é enviado ao Firestore e pode ser refeito a qualquer momento a partir da TMDB.
 */
@Entity(
    tableName = "obrasCache",
    primaryKeys = ["tipoMidia", "tmdbId", "parte"]
)
data class ObraCacheEntity(
    val tipoMidia: String,
    val tmdbId: Int,
    val parte: Int,
    val json: String,
    val atualizadoEm: Long
)