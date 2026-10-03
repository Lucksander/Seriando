package com.seriando.app.ui.mylist

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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seriando.app.data.auth.AuthRepository
import com.seriando.app.data.repository.MinhaListaRepository
import com.seriando.app.model.SerieSalva
import com.seriando.app.model.labelDoStatus
import com.seriando.app.ui.home.SerieCardSalva
import kotlinx.coroutines.launch

@Composable
fun MinhaListaScreen(aoClicarEmItem: (Int, String) -> Unit) {

    val authRepository = remember { AuthRepository() }
    val minhaListaRepository = remember { MinhaListaRepository() }
    val scope = rememberCoroutineScope()
    var minhasSeries by remember { mutableStateOf<List<SerieSalva>>(emptyList()) }

    // null = "Todos"; senão guarda o rótulo de status escolhido (texto, não enum,
    // já que a lista mistura os rótulos de StatusSerie e StatusFilme).
    var filtroLabel by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val uid = authRepository.usuarioAtual()?.uid
        minhasSeries = if (uid != null) minhaListaRepository.buscarSeries(uid) else emptyList()
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

        if (authRepository.usuarioAtual() != null && minhasSeries.isNotEmpty()) {
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
            authRepository.usuarioAtual() == null -> {
                Text(
                    text = "Entre com sua conta na Home para ver suas séries salvas.",
                    style = MaterialTheme.typography.bodyMedium
                )
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
                            val uid = authRepository.usuarioAtual()?.uid ?: return@SerieCardSalva
                            scope.launch {
                                minhaListaRepository.removerSerie(uid, serie.id)
                                minhasSeries = minhasSeries.filter { it.id != serie.id }
                            }
                        }
                    )
                }
            }
        }
    }
}