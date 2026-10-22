package com.seriando.app.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
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
 * Hoje guarda apenas a flag da introdução (RF52). Não depende do login: a introdução é
 * mostrada uma vez por instalação, mesmo que o usuário saia da conta depois.
 * Recebe o Context só para abrir o DataStore (por isso fica ao lado do HistoricoBusca em data/local).
 */
class PreferenciasApp(context: Context) {

    // applicationContext evita segurar uma Activity na memória por engano
    private val dataStore = context.applicationContext.dataStore

    private companion object {
        // Nome da chave conforme a especificação do Bloco L
        val INTRODUCAO_VISTA = booleanPreferencesKey("introducao_vista")
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
}