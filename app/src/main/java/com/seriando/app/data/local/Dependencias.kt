package com.seriando.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import com.seriando.app.data.sync.EstadoDaSincronizacao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Ponto único de acesso ao banco local. Cria o Room uma vez, quando o app abre (SeriandoApp),
 * e entrega os DAOs aos repositórios. Assim os repositórios não precisam de Context (RNF11):
 * recebem só as interfaces dos DAOs.
 */
object Dependencias {

    private lateinit var banco: SeriandoDatabase

    // Contexto do aplicativo (vive o mesmo tempo que o app), usado para apagar a foto de perfil local
    private lateinit var contextoDoApp: Context

    /** Cria o banco. Chamado uma única vez, em SeriandoApp.onCreate. */
    fun iniciar(contexto: Context) {
        contextoDoApp = contexto.applicationContext
        EstadoDaSincronizacao.iniciar(contexto)
        banco = Room.databaseBuilder(
            contexto.applicationContext,
            SeriandoDatabase::class.java,
            "seriando.db"
        ).build()
    }

    val listaDao: ListaDao get() = banco.listaDao()
    val episodioDao: EpisodioDao get() = banco.episodioDao()
    val perfilDao: PerfilDao get() = banco.perfilDao()
    val obraCacheDao: ObraCacheDao get() = banco.obraCacheDao()

    /**
     * Executa várias operações no banco como uma só: ou todas acontecem, ou nenhuma, e
     * nenhuma outra gravação se mistura no meio. A sincronização usa isto para "ler, decidir
     * e gravar" sem o risco de sobrescrever uma alteração que o usuário fez no mesmo instante.
     */
    suspend fun <T> emTransacao(bloco: suspend () -> T): T = banco.withTransaction { bloco() }

    /**
     * Apaga todas as tabelas do banco local (RN25), no logout e na exclusão da conta.
     * Apaga também a foto de perfil guardada no aparelho (RF30), que é dado do usuário.
     * Roda fora da thread principal porque o Room não permite apagar nela.
     */
    suspend fun limparBancoLocal() {
        withContext(Dispatchers.IO) {
            banco.clearAllTables()
            EstadoDaSincronizacao.limpar() // as datas de conferência também são dados do usuário
            FotoDePerfilLocal.remover(contextoDoApp)
        }
    }
}