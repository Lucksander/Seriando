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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.seriando.app.model.StatusFilme
import com.seriando.app.model.StatusSerie
import com.seriando.app.model.OrdemDaLista
import com.seriando.app.model.organizarMinhaLista
import com.seriando.app.ui.home.DialogoRemoverDaLista
import com.seriando.app.ui.home.SerieCardSalva
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableIntStateOf
import com.seriando.app.ui.login.AvisoDeLoginNecessario

/**
 * Aba Minha Lista (RF07, RF08, RF33, RF66): séries e filmes salvos, com filtro por status,
 * busca por nome e ordenação.
 * A lista vem do banco local (Room), que a tela observa: funciona sem internet e se
 * atualiza sozinha quando a sincronização traz mudanças da nuvem (RN21).
 */
@Composable
fun MinhaListaScreen(aoClicarEmItem: (Int, String) -> Unit) {

    val context = LocalContext.current
    val authRepository = remember { AuthRepository() }
    val minhaListaRepository = remember { MinhaListaRepository() }
    val scope = rememberCoroutineScope()
    // Sobe quando o visitante faz login pela folha; como é lido no remember abaixo,
    // força a tela a recompor e trocar o aviso pela lista do usuário
    var loginFeito by remember { mutableIntStateOf(0) }
    val uid = remember(loginFeito) { authRepository.usuarioAtual()?.uid }

    // A lista observada do banco local. null = o banco ainda não respondeu (carregando).
    val minhasSeries by remember { minhaListaRepository.observarSeries() }
        .collectAsState(initial = null)

    // null = "Todos"; senão guarda o rótulo de status escolhido (texto, não enum,
    // já que a lista mistura os rótulos de StatusSerie e StatusFilme).
    var filtroLabel by remember { mutableStateOf<String?>(null) }

    // Busca por nome e ordem da lista (RF66). Ficam só na memória da tela: ao sair da aba,
    // a lista volta ao padrão (sem busca, mais recentes primeiro).
    var busca by remember { mutableStateOf("") }
    var ordem by remember { mutableStateOf(OrdemDaLista.RECENTES) }

    // Item que o usuário tocou em "remover" e ainda não confirmou (null = nenhum diálogo aberto)
    var serieParaRemover by remember { mutableStateOf<SerieSalva?>(null) }

    // Confirmação antes de remover: avisa que o progresso será perdido
    val aRemover = serieParaRemover
    if (aRemover != null && uid != null) {
        DialogoRemoverDaLista(
            tipoMidia = aRemover.tipoMidia,
            aoFechar = { serieParaRemover = null },
            aoConfirmar = {
                serieParaRemover = null
                scope.launch {
                    try {
                        // A tela atualiza sozinha: o item some assim que o banco local muda
                        minhaListaRepository.removerSerie(uid, aRemover.id)
                    } catch (e: Exception) {
                        Log.e("MinhaListaScreen", "Falha ao remover da lista", e)
                        Toast.makeText(context, "Não foi possível remover.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    // Cópia local da lista, para o Kotlin permitir testar null e usar o valor em seguida
    val lista = minhasSeries

    // Chips de filtro: sempre todos os status possíveis, na ordem do ciclo de acompanhamento,
    // e não só os que já aparecem na lista. "Maratonei" é o status de série; "Concluído", o de filme.
    val labelsDosFiltros = listOf(
        StatusSerie.QUERO_ASSISTIR.label,
        StatusSerie.ASSISTINDO.label,
        StatusSerie.COMPLETO.label,   // Maratonei (séries)
        StatusFilme.ASSISTIDO.label,  // Concluído (filmes)
        StatusSerie.ABANDONEI.label
    )

    // Aplica filtro de status, busca por nome e ordem (RF66), nesta ordem
    val seriesFiltradas = organizarMinhaLista(lista.orEmpty(), filtroLabel, busca, ordem)

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

        // Busca, filtro e ordem: só quando há itens para filtrar
        if (uid != null && !lista.isNullOrEmpty()) {
            // Busca por nome (RF66)
            BarraDeBuscaDaLista(texto = busca, aoMudar = { busca = it })
            Spacer(modifier = Modifier.height(12.dp))

            // Chips de filtro por status
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                FilterChip(
                    selected = filtroLabel == null,
                    onClick = { filtroLabel = null },
                    label = { Text("Todos") }
                )
                labelsDosFiltros.forEach { label ->
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = filtroLabel == label,
                        onClick = { filtroLabel = label },
                        label = { Text(label) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Ordem da lista (RF66)
            SeletorDeOrdemDaLista(ordemAtual = ordem, aoEscolher = { ordem = it })
            Spacer(modifier = Modifier.height(16.dp))
        }

        when {
            // Visitante (RF54): aviso com botão "Entrar", que abre a folha de login
            uid == null -> {
                AvisoDeLoginNecessario(
                    mensagem = "Entre com sua conta para salvar e acompanhar suas séries e filmes.",
                    aoLogarComSucesso = { loginFeito++ }
                )
            }
            // Ainda lendo o banco local
            lista == null -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            lista.isEmpty() -> {
                Text(
                    text = "Você ainda não adicionou nenhuma série ou filme.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            seriesFiltradas.isEmpty() -> {
                Text(
                    // Com texto na busca, a mensagem fala da busca; senão, do filtro de status
                    text = if (busca.isNotBlank()) "Nenhum item encontrado para \"${busca.trim()}\"."
                    else "Nenhum item com esse status ainda.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            else -> {
                seriesFiltradas.forEach { serie ->
                    SerieCardSalva(
                        serie = serie,
                        aoClicar = { aoClicarEmItem(serie.id, serie.tipoMidia) },
                        // Em vez de remover direto, pede confirmação (o diálogo fica acima)
                        aoRemover = { serieParaRemover = serie }
                    )
                }
            }
        }
    }
}