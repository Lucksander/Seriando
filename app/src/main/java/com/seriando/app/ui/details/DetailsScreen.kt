package com.seriando.app.ui.details

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.seriando.app.BuildConfig
import com.seriando.app.data.remote.RetrofitInstance
import com.seriando.app.model.SerieDetalhe

// Tela de detalhes de uma série.
// Recebe o ID da série (vindo da navegação) e busca os dados completos
// na TMDB assim que a tela é exibida.
@Composable
fun DetailsScreen(serieId: Int) {

    // Guarda os detalhes carregados; começa nulo (ainda carregando).
    var detalhe by remember { mutableStateOf<SerieDetalhe?>(null) }

    // Guarda se ocorreu erro no carregamento, para mostrar uma mensagem.
    var erro by remember { mutableStateOf(false) }

    // LaunchedEffect roda a busca uma única vez quando a tela abre
    // (ou de novo, se o serieId mudar). É a forma correta de disparar
    // uma chamada suspend a partir de um Composable, sem precisar de botão.
    LaunchedEffect(serieId) {
        try {
            detalhe = RetrofitInstance.api.buscarDetalhesSerie(
                serieId,
                BuildConfig.TMDB_API_KEY
            )
        } catch (e: Exception) {
            Log.e("Seriando", "Erro ao buscar detalhes", e)
            erro = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        when {
            // Enquanto detalhe é nulo e não houve erro, mostra o indicador de carregamento.
            detalhe == null && !erro -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            // Se deu erro, mostra uma mensagem simples.
            erro -> {
                Text("Não foi possível carregar os detalhes da série.")
            }

            // Se os detalhes chegaram, mostra o conteúdo completo.
            else -> {
                val serie = detalhe!!

                AsyncImage(
                    model = serie.caminhoCapa?.let { "https://image.tmdb.org/t/p/w500$it" },
                    contentDescription = serie.nome,
                    modifier = Modifier
                        .width(200.dp) // largura fixa em vez de ocupar a tela toda
                        .aspectRatio(2f / 3f) // mantém a proporção real do pôster da TMDB (2:3), sem distorcer nem cortar
                        .align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = serie.nome,
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = "${serie.numeroTemporadas} temporada(s) · ${serie.numeroEpisodios} episódio(s)",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = serie.sinopse.ifBlank { "Sem sinopse disponível." },
                    style = MaterialTheme.typography.bodyLarge
                )

                // Placeholder: na Etapa 5, entra aqui o botão
                // "Adicionar à minha lista", que grava no Firestore.
            }
        }
    }
}