package com.seriando.app.data.sync

import android.content.Context
import android.content.SharedPreferences

/**
 * Guarda, no aparelho, QUANDO cada série teve os episódios conferidos com a nuvem pela última vez.
 * O Sincronizador usa estas datas para ler da nuvem só o que mudou desde então, em vez de baixar
 * todos os episódios a cada abertura do app (poupa leituras do Firestore).
 *
 * Fica em SharedPreferences, e não no Room, para não mudar tabela nem exigir migração: se este
 * arquivo for apagado, o pior que acontece é uma leitura completa a mais.
 * Cada chave leva o uid do usuário. É limpo junto com o banco local, no logout e na exclusão
 * da conta (Dependencias.limparBancoLocal, RN25).
 */
object EstadoDaSincronizacao {

    private const val ARQUIVO = "estado_sincronizacao"

    private lateinit var prefs: SharedPreferences

    /** Abre o arquivo. Chamado uma vez em Dependencias.iniciar, na abertura do app. */
    fun iniciar(contexto: Context) {
        prefs = contexto.applicationContext.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE)
    }

    private fun chaveConferencia(uid: String, serieId: Int) = "ep_conferido_${uid}_$serieId"
    private fun chaveCompleta(uid: String, serieId: Int) = "ep_completo_${uid}_$serieId"

    /** Última vez (em ms) que os episódios da série foram conferidos com a nuvem; 0 se nunca. */
    fun ultimaConferenciaEpisodios(uid: String, serieId: Int): Long =
        prefs.getLong(chaveConferencia(uid, serieId), 0L)

    /** Última vez (em ms) que TODOS os episódios da série foram lidos da nuvem; 0 se nunca. */
    fun ultimaLeituraCompletaEpisodios(uid: String, serieId: Int): Long =
        prefs.getLong(chaveCompleta(uid, serieId), 0L)

    /** Registra uma conferência por diferença bem-sucedida. */
    fun gravarConferenciaEpisodios(uid: String, serieId: Int, quando: Long) {
        prefs.edit().putLong(chaveConferencia(uid, serieId), quando).apply()
    }

    /** Registra uma leitura completa; ela também vale como conferência. */
    fun gravarLeituraCompletaEpisodios(uid: String, serieId: Int, quando: Long) {
        prefs.edit()
            .putLong(chaveCompleta(uid, serieId), quando)
            .putLong(chaveConferencia(uid, serieId), quando)
            .apply()
    }

    /** Esquece tudo (logout e exclusão de conta): a próxima sincronização lê tudo de novo. */
    fun limpar() {
        prefs.edit().clear().apply()
    }
}
