package com.seriando.app.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.seriando.app.model.Avaliacao
import com.seriando.app.model.EMOCOES_DISPONIVEIS
import com.seriando.app.model.VotoResumo
import com.seriando.app.ui.theme.corDaEmocao
import com.seriando.app.ui.usuario.AvatarDoUsuario

/**
 * Cartão de uma opinião da comunidade (RF21 a RF26, RF35, RF36, RF48).
 * Mostra autor, emoções (chips com bolinha e borda na cor da família), o texto
 * (oculto se for spoiler até o toque) e os botões de votar, denunciar, editar ou remover.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OpiniaoCard(
    opiniao: Avaliacao,
    ehMinha: Boolean,
    resumoVotos: VotoResumo,
    aoEditar: () -> Unit,
    aoRemover: () -> Unit,
    aoCurtir: () -> Unit,
    aoDescurtir: () -> Unit,
    aoDenunciar: () -> Unit
) {
    var revelado by remember { mutableStateOf(!opiniao.spoiler) }

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar do autor: a foto do app que ele escolheu. Quem usa a foto do Google (que não
                // é copiada para a opinião) e avatares antigos aparecem com a inicial do apelido.
                AvatarDoUsuario(
                    avatarId = opiniao.avatarAutor,
                    apelido = opiniao.apelidoAutor,
                    fotoGoogle = null,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.padding(start = 6.dp))
                Text(text = opiniao.apelidoAutor, style = MaterialTheme.typography.titleSmall)
                if (ehMinha && !opiniao.publica) {
                    Spacer(modifier = Modifier.padding(start = 6.dp))
                    Text(text = "(privada)", style = MaterialTheme.typography.labelSmall)
                }
            }

            if (opiniao.emocoes.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    opiniao.emocoes.forEach { idEmocao ->
                        val emocao = EMOCOES_DISPONIVEIS.find { it.id == idEmocao }
                        if (emocao != null) {
                            // Cor já adaptada ao tema claro/escuro
                            val cor = corDaEmocao(emocao)
                            val forma = RoundedCornerShape(16.dp)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(forma)
                                    .background(cor.copy(alpha = 0.18f))
                                    .border(width = 1.dp, color = cor.copy(alpha = 0.7f), shape = forma)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(cor)
                                )
                                Spacer(modifier = Modifier.padding(start = 4.dp))
                                Text(text = emocao.label, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.padding(top = 8.dp))

            if (opiniao.spoiler && !revelado) {
                // Aviso vermelho no lugar do texto; tocar revela (RN07)
                AvisoDeSpoiler(aoRevelar = { revelado = true })
            } else {
                // Já revelado: o selo lembra que o texto abaixo é spoiler
                if (opiniao.spoiler) {
                    SeloDeSpoiler(modifier = Modifier.padding(bottom = 6.dp))
                }
                Text(text = opiniao.texto, style = MaterialTheme.typography.bodyMedium)
            }

            // RN04: o próprio autor não vota na própria opinião, então os
            // botões de curtir/descurtir só aparecem para quem não é o autor.
            if (!ehMinha) {
                Row(modifier = Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = aoCurtir) {
                        Text(
                            text = "👍 ${resumoVotos.curtidas}",
                            style = if (resumoVotos.meuVoto == 1) MaterialTheme.typography.labelLarge
                            else MaterialTheme.typography.labelMedium
                        )
                    }
                    TextButton(onClick = aoDescurtir) {
                        Text(
                            text = "👎 ${resumoVotos.descurtidas}",
                            style = if (resumoVotos.meuVoto == -1) MaterialTheme.typography.labelLarge
                            else MaterialTheme.typography.labelMedium
                        )
                    }
                    TextButton(onClick = aoDenunciar) {
                        Text("Denunciar", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            if (ehMinha) {
                Row(modifier = Modifier.padding(top = 6.dp)) {
                    TextButton(onClick = aoEditar) { Text("Editar") }
                    TextButton(onClick = aoRemover) { Text("Remover") }
                }
            }
        }
    }
}