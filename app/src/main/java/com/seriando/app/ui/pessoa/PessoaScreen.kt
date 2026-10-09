package com.seriando.app.ui.pessoa

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.seriando.app.BuildConfig
import com.seriando.app.data.remote.RetrofitInstance
import com.seriando.app.model.CreditoPessoa
import com.seriando.app.model.PessoaDetalhe

// Converte "AAAA-MM-DD" (formato da TMDB) em "DD/MM/AAAA".
// Retorna null se a data estiver ausente ou fora do formato esperado.
private fun formatarData(iso: String?): String? {
    val partes = iso?.split("-") ?: return null
    return if (partes.size == 3) "${partes[2]}/${partes[1]}/${partes[0]}" else null
}

// Página do ator/atriz (RF41, RF42, RF43): foto, nome, dados de nascimento,
// biografia e filmografia. Tocar num título leva à tela de Detalhes dele.
@Composable
fun PessoaScreen(pessoaId: Int, aoClicarEmItem: (Int, String) -> Unit) {

    var pessoa by remember { mutableStateOf<PessoaDetalhe?>(null) }
    var creditos by remember { mutableStateOf<List<CreditoPessoa>>(emptyList()) }
    var erro by remember { mutableStateOf(false) }
    var biografiaExpandida by remember { mutableStateOf(false) }

    LaunchedEffect(pessoaId) {
        try {
            var dados = RetrofitInstance.api.buscarPessoa(pessoaId, BuildConfig.TMDB_API_KEY)

            // Muitas pessoas não têm biografia em português na TMDB. Nesse caso,
            // buscamos a versão em inglês, para o campo não ficar vazio.
            if (dados.biografia.isBlank()) {
                val emIngles = RetrofitInstance.api.buscarPessoa(pessoaId, BuildConfig.TMDB_API_KEY, "en-US")
                dados = dados.copy(biografia = emIngles.biografia)
            }
            pessoa = dados

            // Filmografia: só filmes/séries com capa, sem repetição, dos mais
            // populares para os menos, para os títulos conhecidos virem primeiro.
            creditos = RetrofitInstance.api.buscarCreditosPessoa(pessoaId, BuildConfig.TMDB_API_KEY).cast
                .filter { it.caminhoCapa != null && (it.tipoMidia == "movie" || it.tipoMidia == "tv") }
                .distinctBy { "${it.tipoMidia}_${it.id}" }
                .sortedByDescending { it.popularity }
        } catch (e: Exception) {
            Log.e("Seriando", "Erro ao buscar pessoa", e)
            erro = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        val dados = pessoa
        when {
            erro -> Text("Não foi possível carregar os dados desta pessoa.")

            dados == null -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))

            else -> {
                AsyncImage(
                    model = dados.caminhoFoto?.let { "https://image.tmdb.org/t/p/w500$it" },
                    contentDescription = dados.nome,
                    modifier = Modifier
                        .width(180.dp)
                        .aspectRatio(2f / 3f)
                        .align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = dados.nome, style = MaterialTheme.typography.headlineMedium)

                formatarData(dados.nascimento)?.let {
                    Text(text = "Nascimento: $it", style = MaterialTheme.typography.bodyMedium)
                }
                dados.localNascimento?.let {
                    Text(text = it, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Biografia", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))

                if (dados.biografia.isBlank()) {
                    Text(text = "Biografia não disponível.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    // Mostra só as primeiras linhas e deixa expandir, já que
                    // algumas biografias da TMDB são muito longas.
                    Text(
                        text = dados.biografia,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = if (biografiaExpandida) Int.MAX_VALUE else 6,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (dados.biografia.length > 300) {
                        TextButton(onClick = { biografiaExpandida = !biografiaExpandida }) {
                            Text(if (biografiaExpandida) "Mostrar menos" else "Ler mais")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "Filmografia", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                if (creditos.isEmpty()) {
                    Text(text = "Nenhum título encontrado.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    // Grade de 3 colunas montada à mão (linhas de 3), porque uma
                    // lista "lazy" não pode ficar dentro de uma Column que já rola.
                    // Limitamos a 60 títulos para não pesar a tela em carreiras longas.
                    creditos.take(60).chunked(3).forEach { linha ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            linha.forEach { credito ->
                                CreditoCard(
                                    credito = credito,
                                    modifier = Modifier.weight(1f),
                                    aoClicar = { aoClicarEmItem(credito.id, credito.tipoMidia ?: "movie") }
                                )
                            }
                            // Preenche as células vazias da última linha, para os
                            // cartões não esticarem quando sobram menos de 3.
                            repeat(3 - linha.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

// Cartão de um título da filmografia: capa, nome e personagem interpretado.
@Composable
private fun CreditoCard(credito: CreditoPessoa, modifier: Modifier = Modifier, aoClicar: () -> Unit) {
    Column(
        modifier = modifier
            .padding(4.dp)
            .clickable { aoClicar() }
    ) {
        AsyncImage(
            model = credito.caminhoCapa?.let { "https://image.tmdb.org/t/p/w185$it" },
            contentDescription = credito.titulo,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f),
            contentScale = ContentScale.Crop
        )
        Text(
            text = credito.titulo,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        credito.personagem?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}