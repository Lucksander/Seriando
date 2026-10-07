package com.seriando.app.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.seriando.app.model.EMOCOES_DISPONIVEIS
import com.seriando.app.model.Emocao
import com.seriando.app.model.MAXIMO_EMOCOES_POR_OPINIAO
import androidx.compose.foundation.layout.size

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

@Composable
private fun ChipEmocao(emocao: Emocao, marcada: Boolean, aoClicar: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(2.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (marcada) Color(emocao.cor).copy(alpha = 0.25f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { aoClicar() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // Bolinha colorida indicando a família da emoção.
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .padding(top = 2.dp, end = 4.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(emocao.cor))
        )
        Text(text = emocao.label, style = MaterialTheme.typography.labelSmall)
    }
}