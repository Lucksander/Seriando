package com.seriando.app.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.seriando.app.model.ModoDoTema
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// Cria UMA única instância do DataStore para o app inteiro (o delegate precisa ficar no nível
// do arquivo; criar duas instâncias para o mesmo arquivo causa erro em tempo de execução).
// O arquivo gerado no aparelho se chama "preferencias_app".
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferencias_app")

/**
 * Preferências do app guardadas SOMENTE no aparelho (nunca vão ao Firestore).
 *
 * Guarda a flag da introdução (RF52) e o modo do tema claro/escuro (RF80). Nenhuma das duas
 * depende do login: a introdução é mostrada uma vez por instalação e o tema escolhido vale
 * mesmo que o usuário saia da conta depois.
 * Recebe o Context só para abrir o DataStore (por isso fica ao lado do HistoricoBusca em data/local).
 */
class PreferenciasApp(context: Context) {

    // applicationContext evita segurar uma Activity na memória por engano
    private val dataStore = context.applicationContext.dataStore

    private companion object {
        // Nome da chave conforme a especificação do Bloco L
        val INTRODUCAO_VISTA = booleanPreferencesKey("introducao_vista")

        // Modo do tema (RF80): guarda a chave do enum ModoDoTema ("sistema", "claro" ou "escuro")
        val MODO_DO_TEMA = stringPreferencesKey("modo_do_tema")

        // Tour guiado: uma flag para a parte da Home e outra para a parte da tela de Detalhes
        val TOUR_HOME_VISTO = booleanPreferencesKey("tour_home_visto")
        val TOUR_DETALHES_VISTO = booleanPreferencesKey("tour_detalhes_visto")
    }

    /**
     * Emite true se a introdução já foi concluída ou pulada, e false na primeira abertura.
     * É um Flow: quem observa recebe o valor novo assim que ele é gravado.
     */
    val introducaoVista: Flow<Boolean> = dataStore.data
        // Se a leitura do arquivo falhar, não derruba o app: trata como "ainda não viu"
        .catch { erro ->
            if (erro is IOException) {
                Log.e("PreferenciasApp", "Falha ao ler as preferências", erro)
                emit(emptyPreferences())
            } else {
                throw erro
            }
        }
        // Chave ausente (primeira abertura) vira false
        .map { preferencias -> preferencias[INTRODUCAO_VISTA] ?: false }

    /**
     * Grava que a introdução foi vista. Chamada ao tocar em "Começar" ou em "Pular" (RF52).
     * É suspend: deve rodar dentro de uma corrotina (por exemplo, rememberCoroutineScope).
     */
    suspend fun marcarIntroducaoVista() {
        try {
            dataStore.edit { preferencias -> preferencias[INTRODUCAO_VISTA] = true }
        } catch (erro: IOException) {
            // Se não conseguir gravar, a introdução reaparece na próxima abertura; não é grave
            Log.e("PreferenciasApp", "Falha ao gravar introducao_vista", erro)
        }
    }

    /**
     * Emite o modo do tema escolhido (RF80). Chave ausente (nunca escolheu) ou desconhecida
     * vira [ModoDoTema.SISTEMA], ou seja, o app segue o aparelho. É um Flow: o tema do app
     * muda na hora quando o usuário escolhe outro modo na aba Conta.
     */
    val modoDoTema: Flow<ModoDoTema> = dataStore.data
        // Falha de leitura não derruba o app: usa o padrão (segue o aparelho)
        .catch { erro ->
            if (erro is IOException) {
                Log.e("PreferenciasApp", "Falha ao ler o modo do tema", erro)
                emit(emptyPreferences())
            } else {
                throw erro
            }
        }
        .map { preferencias -> ModoDoTema.deChave(preferencias[MODO_DO_TEMA]) }

    /**
     * Grava o modo do tema escolhido na aba Conta (RF80).
     * É suspend: deve rodar dentro de uma corrotina (por exemplo, rememberCoroutineScope).
     */
    suspend fun definirModoDoTema(modo: ModoDoTema) {
        try {
            dataStore.edit { preferencias -> preferencias[MODO_DO_TEMA] = modo.chave }
        } catch (erro: IOException) {
            // Se não conseguir gravar, o tema volta ao anterior na próxima abertura; não é grave
            Log.e("PreferenciasApp", "Falha ao gravar modo_do_tema", erro)
        }
    }

    // ---------- Tour guiado ----------

    /**
     * Lê uma flag booleana do DataStore. Chave ausente vira false, e falha de leitura
     * não derruba o app (trata como "ainda não viu"), igual aos outros Flows desta classe.
     */
    private fun lerFlag(chave: Preferences.Key<Boolean>): Flow<Boolean> = dataStore.data
        .catch { erro ->
            if (erro is IOException) {
                Log.e("PreferenciasApp", "Falha ao ler a flag ${chave.name}", erro)
                emit(emptyPreferences())
            } else {
                throw erro
            }
        }
        .map { preferencias -> preferencias[chave] ?: false }

    /** true se o usuário já concluiu ou pulou a parte do tour que acontece na Home. */
    val tourHomeVisto: Flow<Boolean> = lerFlag(TOUR_HOME_VISTO)

    /** true se o usuário já concluiu ou pulou a parte do tour que acontece nos Detalhes. */
    val tourDetalhesVisto: Flow<Boolean> = lerFlag(TOUR_DETALHES_VISTO)

    /** Grava que o tour da Home foi visto (concluído ou pulado). Suspend: chamar numa corrotina. */
    suspend fun marcarTourHomeVisto() {
        try {
            dataStore.edit { preferencias -> preferencias[TOUR_HOME_VISTO] = true }
        } catch (erro: IOException) {
            // Se não conseguir gravar, o tour reaparece na próxima abertura; não é grave
            Log.e("PreferenciasApp", "Falha ao gravar tour_home_visto", erro)
        }
    }

    /** Grava que o tour dos Detalhes foi visto (concluído ou pulado). Suspend: chamar numa corrotina. */
    suspend fun marcarTourDetalhesVisto() {
        try {
            dataStore.edit { preferencias -> preferencias[TOUR_DETALHES_VISTO] = true }
        } catch (erro: IOException) {
            Log.e("PreferenciasApp", "Falha ao gravar tour_detalhes_visto", erro)
        }
    }

    /**
     * Zera as duas flags do tour: é o "Rever tour" da aba Conta. Na próxima vez que a Home
     * e os Detalhes aparecerem, o tour roda de novo.
     */
    suspend fun reiniciarTour() {
        try {
            dataStore.edit { preferencias ->
                preferencias.remove(TOUR_HOME_VISTO)
                preferencias.remove(TOUR_DETALHES_VISTO)
            }
        } catch (erro: IOException) {
            Log.e("PreferenciasApp", "Falha ao reiniciar o tour", erro)
        }
    }
}
