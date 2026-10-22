package com.seriando.app.data.local

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Obra aberta a partir da pesquisa, guardada para a fileira "Vistos recentemente".
 *
 * @param id          id da obra na TMDB.
 * @param tipoMidia   "movie" ou "tv".
 * @param nome        título, mostrado embaixo da capa.
 * @param caminhoCapa caminho relativo da capa na TMDB (pode ser nulo).
 */
data class ObraVista(
    val id: Int,
    val tipoMidia: String,
    val nome: String,
    val caminhoCapa: String?
)

/**
 * Histórico da tela de pesquisa, guardado só no aparelho (SharedPreferences, sem
 * dependência nova): os últimos termos pesquisados e as últimas obras abertas.
 * Nunca vai ao Firestore e não é apagado no logout.
 *
 * Usa Context, por isso é um dos poucos arquivos específicos de Android (RNF11).
 * A lista mais recente vem primeiro e não tem repetição.
 */
class HistoricoBusca(context: Context) {

    // applicationContext: não segura a tela na memória
    private val prefs = context.applicationContext
        .getSharedPreferences("seriando_historico_busca", Context.MODE_PRIVATE)

    // ---------------------------------------------------------------
    // Pesquisas recentes (texto)
    // ---------------------------------------------------------------

    /** Termos pesquisados, do mais recente para o mais antigo. */
    fun pesquisas(): List<String> {
        return try {
            val vetor = JSONArray(prefs.getString(CHAVE_PESQUISAS, null) ?: "[]")
            List(vetor.length()) { vetor.getString(it) }
        } catch (e: Exception) {
            emptyList() // dado corrompido: começa do zero em vez de quebrar a tela
        }
    }

    /** Põe o termo no topo; se já existia (ignorando maiúsculas), ele só sobe. */
    fun registrarPesquisa(termo: String) {
        val limpo = termo.trim()
        if (limpo.isBlank()) return
        val novos = listOf(limpo) + pesquisas().filterNot { it.equals(limpo, ignoreCase = true) }
        gravarPesquisas(novos.take(MAXIMO_PESQUISAS))
    }

    /** Apaga uma pesquisa só. */
    fun removerPesquisa(termo: String) {
        gravarPesquisas(pesquisas().filterNot { it.equals(termo, ignoreCase = true) })
    }

    /** Apaga todas as pesquisas. */
    fun limparPesquisas() {
        prefs.edit().remove(CHAVE_PESQUISAS).apply()
    }

    private fun gravarPesquisas(lista: List<String>) {
        prefs.edit().putString(CHAVE_PESQUISAS, JSONArray(lista).toString()).apply()
    }

    // ---------------------------------------------------------------
    // Obras vistas recentemente (capas)
    // ---------------------------------------------------------------

    /** Obras abertas, da mais recente para a mais antiga. */
    fun obrasVistas(): List<ObraVista> {
        return try {
            val vetor = JSONArray(prefs.getString(CHAVE_OBRAS, null) ?: "[]")
            List(vetor.length()) { posicao ->
                val o = vetor.getJSONObject(posicao)
                ObraVista(
                    id = o.getInt("id"),
                    tipoMidia = o.getString("tipo"),
                    nome = o.getString("nome"),
                    caminhoCapa = if (o.isNull("capa")) null else o.getString("capa")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Põe a obra no topo; filme e série com o mesmo id da TMDB contam como obras diferentes. */
    fun registrarObra(obra: ObraVista) {
        val novas = listOf(obra) +
                obrasVistas().filterNot { it.id == obra.id && it.tipoMidia == obra.tipoMidia }
        val vetor = JSONArray()
        novas.take(MAXIMO_OBRAS).forEach {
            vetor.put(
                JSONObject()
                    .put("id", it.id)
                    .put("tipo", it.tipoMidia)
                    .put("nome", it.nome)
                    .put("capa", it.caminhoCapa ?: JSONObject.NULL)
            )
        }
        prefs.edit().putString(CHAVE_OBRAS, vetor.toString()).apply()
    }

    /** Apaga todas as obras vistas. */
    fun limparObras() {
        prefs.edit().remove(CHAVE_OBRAS).apply()
    }

    private companion object {
        const val CHAVE_PESQUISAS = "pesquisas"
        const val CHAVE_OBRAS = "obras"
        const val MAXIMO_PESQUISAS = 10
        const val MAXIMO_OBRAS = 12
    }
}