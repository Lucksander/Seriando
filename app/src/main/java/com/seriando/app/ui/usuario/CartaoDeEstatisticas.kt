package com.seriando.app.ui.usuario

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.seriando.app.model.EstatisticasDoUsuario

/**
 * Seção "Minha atividade" da aba Conta (RF67): três números lado a lado, cada um com ícone,
 * valor grande e legenda: filmes concluídos, séries maratonadas e episódios assistidos.
 *
 * Só mostra os números; quem os calcula é o MinhaListaRepository.observarEstatisticas().
 *
 * @param estatisticas contagens atuais (todas zeradas para quem ainda não marcou nada).
 */
@Composable
fun CartaoDeEstatisticas(estatisticas: EstatisticasDoUsuario) {
    CartaoDeSecao(titulo = "Minha atividade") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // weight(1f) em cada coluna: as três dividem a largura em partes iguais
            NumeroDeEstatistica(
                icone = Icons.Outlined.Movie,
                valor = estatisticas.filmesConcluidos,
                legenda = plural(estatisticas.filmesConcluidos, "filme concluído", "filmes concluídos"),
                modifier = Modifier.weight(1f)
            )
            NumeroDeEstatistica(
                icone = Icons.Outlined.LiveTv,
                valor = estatisticas.seriesMaratonadas,
                legenda = plural(estatisticas.seriesMaratonadas, "série maratonada", "séries maratonadas"),
                modifier = Modifier.weight(1f)
            )
            NumeroDeEstatistica(
                icone = Icons.Outlined.CheckCircle,
                valor = estatisticas.episodiosAssistidos,
                legenda = plural(estatisticas.episodiosAssistidos, "episódio assistido", "episódios assistidos"),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** Uma coluna do cartão: ícone na cor da marca, número em destaque e legenda embaixo. */
@Composable
private fun NumeroDeEstatistica(
    icone: ImageVector,
    valor: Int,
    legenda: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icone,
            contentDescription = null, // decorativo: a legenda já diz o que é
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = valor.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = legenda,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/** Escolhe a legenda no singular (exatamente 1) ou no plural (0, 2, 3...). */
private fun plural(quantidade: Int, singular: String, plural: String): String =
    if (quantidade == 1) singular else plural
