package com.seriando.app.ui.details

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.seriando.app.model.EMOCOES_DISPONIVEIS
import com.seriando.app.model.Emocao
import com.seriando.app.model.MAXIMO_EMOCOES_POR_OPINIAO
import com.seriando.app.ui.theme.corDaEmocao

/**
 * Seletor das emoções da opinião (RF35, RN10, RN11).
 *
 * As 24 emoções aparecem agrupadas por família de cor (verde, amarelo, laranja, vermelho, azul,
 * rosa e cinza), cada grupo numa fileira que quebra linha sozinha conforme a largura da tela.
 * No rodapé, um medidor de bolinhas mostra quantas das [MAXIMO_EMOCOES_POR_OPINIAO] já foram usadas.
 *
 * Ao atingir o limite, as emoções ainda não escolhidas ficam esmaecidas e param de responder ao
 * toque; é preciso desmarcar uma antes de marcar outra.
 *
 * @param selecionadas ids das emoções escolhidas (estado mantido por quem chama).
 * @param aoMudar      recebe a nova lista de ids a cada marcação ou desmarcação.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SeletorEmocoes(selecionadas: List<String>, aoMudar: (List<String>) -> Unit) {
    val limiteAtingido = selecionadas.size >= MAXIMO_EMOCOES_POR_OPINIAO

    Column {
        // Agrupa pelas cores do model (a lista já vem ordenada por família). O LinkedHashMap
        // que o groupBy devolve mantém a ordem original dos grupos.
        EMOCOES_DISPONIVEIS.groupBy { it.cor }.values.forEach { grupo ->
            FlowRow(
                modifier = Modifier.padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                grupo.forEach { emocao ->
                    val marcada = selecionadas.contains(emocao.id)
                    ChipEmocao(
                        emocao = emocao,
                        marcada = marcada,
                        // Esmaecida: limite atingido e esta emoção não está marcada
                        bloqueada = limiteAtingido && !marcada,
                        aoAlternar = {
                            if (marcada) {
                                aoMudar(selecionadas - emocao.id)
                            } else if (!limiteAtingido) {
                                aoMudar(selecionadas + emocao.id)
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        MedidorDeEmocoes(usadas = selecionadas.size)
    }
}

/**
 * Medidor do limite: uma bolinha por vaga, preenchida na cor principal quando usada,
 * seguida do texto "3 de 6 emoções".
 */
@Composable
private fun MedidorDeEmocoes(usadas: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(MAXIMO_EMOCOES_POR_OPINIAO) { posicao ->
            val cheia = posicao < usadas
            Box(
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (cheia) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    )
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$usadas de $MAXIMO_EMOCOES_POR_OPINIAO emoções",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Chip de uma emoção. A cor vem de corDaEmocao (já adaptada ao tema claro/escuro).
 *
 * Não marcada: fundo de superfície, borda fina na cor da família e bolinha colorida.
 * Marcada: fundo colorido, borda mais grossa e um "check" no lugar da bolinha, para o estado
 * ficar óbvio sem depender só da cor. A mudança de cor é animada.
 * Bloqueada (limite atingido): esmaecida e sem resposta ao toque.
 */
@Composable
private fun ChipEmocao(
    emocao: Emocao,
    marcada: Boolean,
    bloqueada: Boolean,
    aoAlternar: () -> Unit
) {
    val cor = corDaEmocao(emocao)
    val forma = RoundedCornerShape(20.dp)

    // Cores animadas ao marcar/desmarcar
    val fundo by animateColorAsState(
        targetValue = if (marcada) cor.copy(alpha = 0.28f) else MaterialTheme.colorScheme.surfaceVariant,
        label = "fundoDoChip"
    )
    val borda by animateColorAsState(
        targetValue = if (marcada) cor else cor.copy(alpha = 0.45f),
        label = "bordaDoChip"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .alpha(if (bloqueada) 0.4f else 1f)
            // Altura mínima de 36dp para o toque ficar confortável
            .heightIn(min = 36.dp)
            .clip(forma)
            .background(fundo)
            .border(width = if (marcada) 1.5.dp else 1.dp, color = borda, shape = forma)
            // toggleable informa ao TalkBack que é um item marcável; enabled=false quando bloqueada
            .toggleable(
                value = marcada,
                enabled = !bloqueada,
                role = Role.Checkbox,
                onValueChange = { aoAlternar() }
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        if (marcada) {
            // Marcada: check na cor da família
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null, // o estado já é anunciado pelo toggleable
                tint = cor,
                modifier = Modifier.size(14.dp)
            )
        } else {
            // Não marcada: bolinha colorida da família
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(cor)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = emocao.label, style = MaterialTheme.typography.labelLarge)
    }
}
