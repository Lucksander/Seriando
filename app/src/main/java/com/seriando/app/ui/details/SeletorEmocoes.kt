package com.seriando.app.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.seriando.app.model.EMOCOES_DISPONIVEIS
import com.seriando.app.model.Emocao
import com.seriando.app.model.MAXIMO_EMOCOES_POR_OPINIAO
import com.seriando.app.ui.theme.corDaEmocao

// Grade de chips de emoção, em fileiras de 4. Respeita o limite de
// MAXIMO_EMOCOES_POR_OPINIAO: ao atingir o limite, novos toques em
// emoções não selecionadas são ignorados (precisa desmarcar outra antes).
@Composable
fun SeletorEmocoes(selecionadas: List<String>, aoMudar: (List<String>) -> Unit) {
    Column {
        EMOCOES_DISPONIVEIS.chunked(4).forEach { linha ->
            Row {
                linha.forEach { emocao ->
                    val marcada = selecionadas.contains(emocao.id)
                    ChipEmocao(
                        emocao = emocao,
                        marcada = marcada,
                        aoClicar = {
                            if (marcada) {
                                aoMudar(selecionadas - emocao.id)
                            } else if (selecionadas.size < MAXIMO_EMOCOES_POR_OPINIAO) {
                                aoMudar(selecionadas + emocao.id)
                            }
                            // Se já atingiu o limite e a emoção não está marcada, o toque não faz nada.
                        }
                    )
                }
            }
        }
        Text(
            text = "${selecionadas.size}/$MAXIMO_EMOCOES_POR_OPINIAO emoções escolhidas",
            style = MaterialTheme.typography.labelSmall
        )
    }
}

/**
 * Chip de uma emoção. A cor vem de corDaEmocao (já adaptada ao tema claro/escuro).
 * Não escolhida: fundo de superfície e borda fina na cor da família, discreta.
 * Escolhida: fundo colorido translúcido e borda cheia, para o estado ficar óbvio.
 */
@Composable
private fun ChipEmocao(emocao: Emocao, marcada: Boolean, aoClicar: () -> Unit) {
    val cor = corDaEmocao(emocao)
    val forma = RoundedCornerShape(16.dp)

    Row(
        modifier = Modifier
            .padding(2.dp)
            .clip(forma)
            .background(
                if (marcada) cor.copy(alpha = 0.25f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(
                width = 1.dp,
                color = if (marcada) cor else cor.copy(alpha = 0.45f),
                shape = forma
            )
            .clickable { aoClicar() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // Bolinha colorida indicando a família da emoção.
        Box(
            modifier = Modifier
                .padding(top = 2.dp, end = 4.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(cor)
        )
        Text(text = emocao.label, style = MaterialTheme.typography.labelSmall)
    }
}