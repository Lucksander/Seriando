package com.seriando.app.ui.tour

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/** Tempo máximo esperando o alvo de um passo aparecer na tela antes de pular o passo. */
private const val ESPERA_DO_ALVO_MS = 1500L

/**
 * Camada do tour: escurece a tela inteira, abre um "buraco" em volta do elemento do passo atual
 * e mostra um balão com título, texto e os botões Próximo e Pular.
 *
 * Deve ser colocada POR CIMA de todo o conteúdo (último filho de um Box que ocupa a tela toda),
 * para o escurecimento cobrir também a barra inferior.
 *
 * Comportamento:
 *  - Enquanto o tour está ativo, toques na tela não chegam ao app por baixo.
 *  - Antes de destacar um alvo, executa o [PassoDoTour.aoMostrar] (rolagem, por exemplo) e espera
 *    o elemento aparecer. Se ele não aparecer em [ESPERA_DO_ALVO_MS], o passo é pulado.
 *  - A tecla Voltar do Android pula o tour.
 *
 * @param estado     estado do tour (passos e posições dos alvos).
 * @param aoEncerrar chamado uma vez quando o tour termina (concluído ou pulado). Recebe true se
 *                   pelo menos um passo chegou a ser exibido; só nesse caso vale gravar a flag
 *                   de "tour visto", senão o usuário perderia o tour sem ter visto nada.
 */
@Composable
fun TourOverlay(
    estado: TourState,
    aoEncerrar: (viuAlgumPasso: Boolean) -> Unit
) {
    // Tour parado: não desenha nada
    val passo = estado.passoAtual ?: return

    // Pula o tour pela tecla Voltar, em vez de fechar a tela por baixo
    BackHandler(enabled = true) {
        estado.encerrar()
        aoEncerrar(estado.mostrados > 0)
    }

    // A cada passo: prepara a tela (rolagem) e espera o alvo aparecer.
    // Se ele nunca aparecer (dados não carregaram, item inexistente), segue para o próximo.
    LaunchedEffect(estado.indice) {
        // Uma falha ao preparar o passo (rolagem, por exemplo) não pode derrubar o app: registra
        // e segue; se o alvo não aparecer, o passo é pulado logo abaixo. O cancelamento da
        // corrotina (o passo mudou ou a tela saiu) precisa continuar se propagando.
        try {
            passo.aoMostrar?.invoke()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("TourOverlay", "Falha ao preparar o passo ${passo.alvoId}", e)
        }
        val retanguloDoAlvo = withTimeoutOrNull(ESPERA_DO_ALVO_MS) {
            snapshotFlow { estado.alvos[passo.alvoId] }
                .filter { it != null && !it.isEmpty }
                .first()
        }
        if (retanguloDoAlvo != null) {
            estado.registrarPassoMostrado()
        } else if (!estado.avancar()) {
            aoEncerrar(estado.mostrados > 0)
        }
    }

    // Origem desta camada na janela: as posições dos alvos são da janela, então são
    // convertidas para coordenadas locais subtraindo esta origem (cobre barra de status etc.)
    var origem by remember { mutableStateOf(Offset.Zero) }

    // Alvo do passo atual em coordenadas locais; null enquanto ainda não apareceu
    val alvo = estado.alvos[passo.alvoId]
        ?.takeIf { !it.isEmpty }
        ?.translate(-origem.x, -origem.y)

    val corDoDestaque = MaterialTheme.colorScheme.primary
    val densidade = LocalDensity.current
    val ehUltimoPasso = estado.indice == estado.passos.lastIndex

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { origem = it.positionInRoot() }
            // Consome todos os toques: o app por baixo fica inerte durante o tour
            .pointerInput(Unit) { detectTapGestures { } }
    ) {
        val alturaDaTela = constraints.maxHeight.toFloat()

        // ---------- Fundo escuro com recorte no alvo ----------
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                // Offscreen é obrigatório para o BlendMode.Clear furar só o escurecimento
                // e não a janela inteira
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        ) {
            drawRect(color = Color.Black.copy(alpha = 0.78f))
            if (alvo != null) {
                val folga = 6.dp.toPx() // respiro entre o elemento e a borda do recorte
                val topoEsquerdo = Offset(alvo.left - folga, alvo.top - folga)
                val tamanho = Size(alvo.width + folga * 2, alvo.height + folga * 2)
                val raio = CornerRadius(14.dp.toPx())
                // Fura o escurecimento no formato do alvo
                drawRoundRect(
                    color = Color.Black,
                    topLeft = topoEsquerdo,
                    size = tamanho,
                    cornerRadius = raio,
                    blendMode = BlendMode.Clear
                )
                // Contorno colorido em volta do recorte
                drawRoundRect(
                    color = corDoDestaque,
                    topLeft = topoEsquerdo,
                    size = tamanho,
                    cornerRadius = raio,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // ---------- Balão ----------
        // Sem alvo ainda (rolando ou carregando), só o fundo escuro aparece
        if (alvo != null) {
            val margem = with(densidade) { 20.dp.toPx() }
            val espacoMinimo = with(densidade) { 200.dp.toPx() } // altura aproximada do balão
            val espacoAbaixo = alturaDaTela - alvo.bottom
            val espacoAcima = alvo.top

            // Põe o balão do lado que tiver mais espaço; se nenhum lado couber (alvo muito alto),
            // ancora embaixo, por cima do alvo
            val abaixo = espacoAbaixo >= espacoMinimo && (espacoAbaixo >= espacoAcima || espacoAcima < espacoMinimo)
            val acima = !abaixo && espacoAcima >= espacoMinimo

            val posicao = when {
                abaixo -> Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = with(densidade) { (alvo.bottom + margem).toDp() })
                acima -> Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = with(densidade) { (alturaDaTela - alvo.top + margem).toDp() })
                else -> Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            }

            Card(
                modifier = posicao
                    .padding(horizontal = 24.dp)
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    // Leitores de tela anunciam o novo texto a cada passo
                    .semantics { liveRegion = LiveRegionMode.Polite },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Contador: "2 de 6"
                    Text(
                        text = "${estado.indice + 1} de ${estado.passos.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = passo.titulo, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = passo.texto, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // No último passo não há o que pular; o espaço fica vazio para o
                        // botão principal não mudar de lugar
                        if (!ehUltimoPasso) {
                            TextButton(onClick = {
                                estado.encerrar()
                                aoEncerrar(estado.mostrados > 0)
                            }) { Text("Pular") }
                        } else {
                            Spacer(modifier = Modifier.height(1.dp))
                        }

                        Button(onClick = {
                            if (!estado.avancar()) aoEncerrar(estado.mostrados > 0)
                        }) {
                            Text(if (ehUltimoPasso) "Concluir" else "Próximo")
                        }
                    }
                }
            }
        }
    }
}
