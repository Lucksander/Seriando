package com.seriando.app.ui.search

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seriando.app.BuildConfig
import com.seriando.app.data.cache.BuscaCache
import com.seriando.app.data.remote.RetrofitInstance
import com.seriando.app.model.ResultadoBusca
import com.seriando.app.ui.home.ResultadoCard
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(aoClicarEmItem: (Int, String) -> Unit) {

    // O valor inicial vem do cache, não de uma string vazia — assim,
    // se o usuário já pesquisou algo antes e voltou para cá, o campo
    // e os resultados aparecem como estavam.
    var textoPesquisa by remember { mutableStateOf(BuscaCache.textoPesquisa) }
    var resultados by remember { mutableStateOf(BuscaCache.resultados) }
    val scope = rememberCoroutineScope()

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
            onClick = {
                scope.launch {
                    try {
                        val resposta = RetrofitInstance.api.buscarMulti(
                            BuildConfig.TMDB_API_KEY,
                            textoPesquisa
                        )
                        resultados = resposta.results.filter { it.ehFilme || it.ehSerie }
                        BuscaCache.resultados = resultados // salva o resultado também
                    } catch (e: Exception) {
                        Log.e("Seriando", "Erro na pesquisa", e)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Pesquisar")
        }

        Spacer(modifier = Modifier.height(16.dp))

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