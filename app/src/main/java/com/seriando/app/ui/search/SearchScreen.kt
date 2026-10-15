package com.seriando.app.ui.search

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.seriando.app.BuildConfig
import com.seriando.app.data.cache.BuscaCache
import com.seriando.app.data.remote.RetrofitInstance
import com.seriando.app.model.ResultadoBusca
import com.seriando.app.ui.home.ResultadoCard
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import com.seriando.app.data.sync.MonitorConexao
import com.seriando.app.ui.comum.MensagemSemConexao

/**
 * Tela de pesquisa de séries e filmes (RF03, RF04).
 * Estados: pesquisando (indicador), falha (mensagem + Toast), nenhum resultado
 * e lista de resultados. O texto e os resultados ficam no BuscaCache para
 * sobreviver à ida e volta entre telas.
 *
 * @param aoClicarEmItem abre os Detalhes (id e tipo "movie"/"tv").
 */
@Composable
fun SearchScreen(aoClicarEmItem: (Int, String) -> Unit) {

    val context = LocalContext.current

    // true se há internet agora (muda o texto da mensagem de falha)
    val online by MonitorConexao.online.collectAsState()

    // O valor inicial vem do cache, não de uma string vazia — assim,
    // se o usuário já pesquisou algo antes e voltou para cá, o campo
    // e os resultados aparecem como estavam.
    var textoPesquisa by remember { mutableStateOf(BuscaCache.textoPesquisa) }
    var resultados by remember { mutableStateOf(BuscaCache.resultados) }
    val scope = rememberCoroutineScope()

    var pesquisando by remember { mutableStateOf(false) }   // chamada à TMDB em andamento
    var falhou by remember { mutableStateOf(false) }        // a última pesquisa deu erro
    // Distingue "ainda não pesquisei" de "pesquisei e não achei nada".
    // Começa true se já há resultados guardados no cache.
    var jaPesquisou by remember { mutableStateOf(BuscaCache.resultados.isNotEmpty()) }

    /** Executa a pesquisa com o texto atual, tratando erro, carregamento e texto vazio. */
    fun pesquisar() {
        // Texto vazio: não gasta chamada à API, só avisa
        if (textoPesquisa.isBlank()) {
            Toast.makeText(context, "Digite o nome de uma série ou filme.", Toast.LENGTH_SHORT).show()
            return
        }
        if (pesquisando) return // evita toques repetidos

        pesquisando = true
        falhou = false
        scope.launch {
            try {
                val resposta = RetrofitInstance.api.buscarMulti(
                    BuildConfig.TMDB_API_KEY,
                    textoPesquisa.trim()
                )
                resultados = resposta.results.filter { it.ehFilme || it.ehSerie }
                BuscaCache.resultados = resultados // salva o resultado também
                jaPesquisou = true
            } catch (e: Exception) {
                Log.e("Seriando", "Erro na pesquisa", e)
                falhou = true
                Toast.makeText(context, "Não foi possível pesquisar.", Toast.LENGTH_SHORT).show()
            } finally {
                pesquisando = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        OutlinedTextField(
            value = textoPesquisa,
            onValueChange = {
                textoPesquisa = it
                BuscaCache.textoPesquisa = it // mantém o cache atualizado a cada letra digitada
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Pesquisar série ou filme") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { pesquisar() },
            enabled = !pesquisando, // desabilitado durante a busca
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (pesquisando) "Pesquisando..." else "Pesquisar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            pesquisando -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            falhou -> {
                MensagemSemConexao(
                    texto = if (!online) {
                        "Sem conexão com a internet. A pesquisa precisa de internet."
                    } else {
                        "Não foi possível pesquisar agora."
                    },
                    aoTentarDeNovo = { pesquisar() }
                )
            }
            jaPesquisou && resultados.isEmpty() -> {
                Text(
                    text = "Nenhum resultado para essa pesquisa.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            else -> {
                resultados.forEach { resultado ->
                    ResultadoCard(
                        resultado = resultado,
                        aoClicar = {
                            val tipoMidia = if (resultado.ehFilme) "movie" else "tv"
                            aoClicarEmItem(resultado.id, tipoMidia)
                        }
                    )
                }
            }
        }
    }
}