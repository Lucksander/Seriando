package com.seriando.app.ui.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Cartão selecionável de uma temporada, na fileira horizontal da tela de Detalhes.
 * Mostra o nome, a quantidade de episódios, o ano e uma barra com o progresso assistido.
 * O cartão selecionado fica preenchido com a cor principal do tema.
 *
 * @param nome           nome da temporada (ex: "Temporada 1").
 * @param totalEpisodios quantidade de episódios da temporada.
 * @param ano            ano de exibição, ou null se a TMDB não informar.
 * @param assistidos     episódios já marcados como assistidos nesta temporada.
 * @param selecionada    true para o cartão da temporada aberta.
 * @param aoClicar       chamado ao tocar no cartão.
 */
@Composable
fun CartaoTemporada(
    nome: String,
    totalEpisodios: Int,
    ano: String?,
    assistidos: Int,
    selecionada: Boolean,
    aoClicar: () -> Unit
) {
    val cores = MaterialTheme.colorScheme
    // Cores do cartão mudam conforme a seleção
    val corFundo = if (selecionada) cores.primary else cores.surfaceVariant
    val corTexto = if (selecionada) cores.onPrimary else cores.onSurface
    val corBarra = if (selecionada) cores.onPrimary else cores.primary
    val corTrilho = if (selecionada) cores.onPrimary.copy(alpha = 0.3f) else cores.onSurface.copy(alpha = 0.12f)

    Surface(
        onClick = aoClicar,
        shape = RoundedCornerShape(12.dp),
        color = corFundo,
        contentColor = corTexto,
        // Só o cartão não selecionado ganha um fio de borda
        border = if (selecionada) null else BorderStroke(1.dp, cores.outlineVariant),
        modifier = Modifier.width(170.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = nome,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                minLines = 2, // reserva 2 linhas: todos os cartões ficam com a mesma altura
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            val episodios = if (totalEpisodios == 1) "1 episódio" else "$totalEpisodios episódios"
            Text(
                text = listOfNotNull(episodios, ano).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall
            )

            // Sem episódios não há o que mostrar de progresso (evita divisão por zero)
            if (totalEpisodios > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (assistidos.toFloat() / totalEpisodios).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = corBarra,
                    trackColor = corTrilho
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$assistidos/$totalEpisodios assistidos",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}