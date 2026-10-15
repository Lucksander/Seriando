package com.seriando.app.ui.comum

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.seriando.app.data.sync.MonitorConexao
import com.seriando.app.data.sync.Sincronizador
import kotlinx.coroutines.delay

/**
 * Envolve o app com uma faixa no topo que informa o estado da conexão e da sincronização (RF62):
 *  - sem internet: avisa que o app segue funcionando com o que está salvo no aparelho e, se
 *    houver, quantas alterações ainda não foram sincronizadas;
 *  - com internet mas alterações pendentes por mais de 3 segundos: mostra a contagem e o botão
 *    "Enviar agora". O atraso evita a faixa piscar a cada toque, já que toda alteração fica
 *    pendente por um instante até ser enviada.
 * Sem nada a avisar, a faixa não aparece e o conteúdo ocupa a tela toda.
 *
 * @param conteudo o app (o grafo de navegação), desenhado abaixo da faixa.
 */
@Composable
fun ComFaixaDeConexao(conteudo: @Composable () -> Unit) {

    val online by MonitorConexao.online.collectAsState()
    val pendentes by remember { Sincronizador.observarPendentes() }.collectAsState(initial = 0)
    val sincronizando by Sincronizador.sincronizando.collectAsState()

    // Online com pendências e sem sincronizar agora: só avisa se isso durar 3 segundos
    val aguardandoEnvio = online && pendentes > 0 && !sincronizando
    var avisarPendentes by remember { mutableStateOf(false) }
    LaunchedEffect(aguardandoEnvio) {
        avisarPendentes = false
        if (aguardandoEnvio) {
            delay(3_000)
            avisarPendentes = true
        }
    }

    val mensagem: String? = when {
        !online && pendentes > 0 -> "Sem conexão. ${descreverPendentes(pendentes)}."
        !online -> "Sem conexão. Você pode continuar usando o que está salvo no aparelho."
        avisarPendentes && pendentes > 0 -> "${descreverPendentes(pendentes)}."
        else -> null
    }

    Column(modifier = Modifier.fillMaxSize()) {

        if (mensagem != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .statusBarsPadding() // a faixa fica abaixo da barra de status, com o fundo por trás dela
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (online) Icons.Filled.Refresh else Icons.Filled.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = mensagem,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f)
                )
                if (online) {
                    TextButton(onClick = {
                        // Pede a sincronização completa (envia as pendências e baixa a nuvem)
                        FirebaseAuth.getInstance().currentUser?.uid?.let { Sincronizador.disparar(it) }
                    }) {
                        Text("Enviar agora")
                    }
                }
            }
        }

        // Com a faixa visível, ela já ocupou a área da barra de status: o conteúdo abaixo
        // "consome" esse espaço para as telas não o duplicarem
        Box(
            modifier = Modifier
                .weight(1f)
                .then(
                    if (mensagem != null) Modifier.consumeWindowInsets(WindowInsets.statusBars)
                    else Modifier
                )
        ) {
            conteudo()
        }
    }
}

/** "1 alteração ainda não sincronizada" ou "N alterações ainda não sincronizadas". */
private fun descreverPendentes(quantidade: Int): String =
    if (quantidade == 1) "1 alteração ainda não sincronizada"
    else "$quantidade alterações ainda não sincronizadas"