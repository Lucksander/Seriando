package com.seriando.app.ui.mylist

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.seriando.app.data.auth.AuthRepository
import com.seriando.app.data.repository.MinhaListaRepository
import com.seriando.app.model.SerieSalva
import com.seriando.app.model.labelDoStatus
import com.seriando.app.ui.home.SerieCardSalva
import kotlinx.coroutines.launch

/**
 * Aba Minha Lista (RF07, RF08): séries e filmes salvos, com filtro por status.
 * Tem três estados além do normal: carregando, falha (com botão para tentar de novo)
 * e sem login.
 */
@Composable
fun MinhaListaScreen(aoClicarEmItem: (Int, String) -> Unit) {

    val context = LocalContext.current
    val authRepository = remember { AuthRepository() }
    val minhaListaRepository = remember { MinhaListaRepository() }
    val scope = rememberCoroutineScope()
    val uid = authRepository.usuarioAtual()?.uid

    var minhasSeries by remember { mutableStateOf<List<SerieSalva>>(emptyList()) }
    var carregando by remember { mutableStateOf(true) }   // busca da lista em andamento
    var falhou by remember { mutableStateOf(false) }      // a busca deu erro

    // Incrementar este número refaz a busca (botão "Tentar novamente")
    var tentativa by remember { mutableIntStateOf(0) }

    // null = "Todos"; senão guarda o rótulo de status escolhido (texto, não enum,
    // já que a lista mistura os rótulos de StatusSerie e StatusFilme).
    var filtroLabel by remember { mutableStateOf<String?>(null) }

    // Busca a lista ao abrir, quando o login muda ou quando o usuário tenta de novo.
    LaunchedEffect(uid, tentativa) {
        falhou = false
        if (uid == null) {
            // Sem login não há o que buscar (e o Firestore recusaria a leitura)
            minhasSeries = emptyList()
            carregando = false
            return@LaunchedEffect
        }
        carregando = true
        try {
            minhasSeries = minhaListaRepository.buscarSeries(uid)
        } catch (e: Exception) {
            Log.e("MinhaListaScreen", "Falha ao buscar a lista", e)
            falhou = true
        } finally {
            carregando = false
        }
    }

    // Monta a lista de rótulos realmente presentes na lista do usuário,
    // sem repetir, para os chips de filtro mostrarem só o que faz sentido.
    val labelsPresentes = minhasSeries
        .map { labelDoStatus(it.tipoMidia, it.status) }
        .distinct()

    val seriesFiltradas = if (filtroLabel == null) {
        minhasSeries
    } else {
        minhasSeries.filter { labelDoStatus(it.tipoMidia, it.status) == filtroLabel }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Minha Lista",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Chips de filtro: só quando há itens para filtrar
        if (uid != null && minhasSeries.isNotEmpty()) {
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                FilterChip(
                    selected = filtroLabel == null,
                    onClick = { filtroLabel = null },
                    label = { Text("Todos") }
                )
                labelsPresentes.forEach { label ->
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = filtroLabel == label,
                        onClick = { filtroLabel = label },
                        label = { Text(label) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        when {
            uid == null -> {
                Text(
                    text = "Entre com sua conta na Home para ver suas séries salvas.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            // Carregando: indicador, em vez de mostrar "lista vazia" por engano
            carregando -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            // Falha: mensagem e botão para tentar de novo
            falhou -> {
                Text(
                    text = "Não foi possível carregar sua lista.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { tentativa++ }) {
                    Text("Tentar novamente")
                }
            }
            minhasSeries.isEmpty() -> {
                Text(
                    text = "Você ainda não adicionou nenhuma série ou filme.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            seriesFiltradas.isEmpty() -> {
                Text(
                    text = "Nenhum item com esse status ainda.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            else -> {
                seriesFiltradas.forEach { serie ->
                    SerieCardSalva(
                        serie = serie,
                        aoClicar = { aoClicarEmItem(serie.id, serie.tipoMidia) },
                        aoRemover = {
                            scope.launch {
                                try {
                                    minhaListaRepository.removerSerie(uid, serie.id)
                                    minhasSeries = minhasSeries.filter { it.id != serie.id }
                                } catch (e: Exception) {
                                    Log.e("MinhaListaScreen", "Falha ao remover da lista", e)
                                    Toast.makeText(context, "Não foi possível remover.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}