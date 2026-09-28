package com.seriando.app.ui.home

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
import androidx.compose.material3.MaterialTheme
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
import com.seriando.app.data.remote.RetrofitInstance
import com.seriando.app.model.Serie
import kotlinx.coroutines.launch

@Composable
fun HomeScreen() {

    var textoPesquisa by remember { mutableStateOf("") }
    var resultados by remember { mutableStateOf<List<Serie>>(emptyList()) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "SERIANDO",
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "Organize suas séries. Nunca perca onde parou.",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = textoPesquisa,
            onValueChange = { textoPesquisa = it },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Pesquisar série")
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                scope.launch {
                    try {
                        val resposta = RetrofitInstance.api.buscarSeries(
                            BuildConfig.TMDB_API_KEY,
                            textoPesquisa
                        )
                        resultados = resposta.results
                    } catch (e: Exception) {
                        Log.e("Seriando", "Erro na pesquisa", e)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Pesquisar")
        }

        resultados.forEach { serie ->
            SerieCard(serie)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Minhas séries",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Você ainda não adicionou nenhuma série.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}