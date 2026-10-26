package com.seriando.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.seriando.app.model.ContinuarAssistindo
import com.seriando.app.ui.theme.capaDeObra

// Largura de cada cartão da fileira; a altura se ajusta ao conteúdo
private val larguraCartao = 280.dp

/**
 * Fileira "Continuar assistindo" da Home (RF83): as séries com status "Assistindo", com o
 * próximo episódio e uma barra de progresso. Tocar em um cartão abre os detalhes da série.
 *
 * É um componente "burro": só desenha o que a HomeScreen entrega. Quando não há séries
 * em andamento, quem chama simplesmente não a desenha.
 *
 * @param itens          séries em andamento, da mais recente para a mais antiga.
 * @param aoClicarEmItem toque em um cartão: recebe o id e o tipo ("tv").
 */
@Composable
fun FileiraContinuarAssistindo(
    itens: List<ContinuarAssistindo>,
    aoClicarEmItem: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Continuar assistindo",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            // Mesma margem das outras fileiras
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(itens, key = { it.serie.id }) { item ->
                CartaoContinuarAssistindo(
                    item = item,
                    aoClicar = { aoClicarEmItem(item.serie.id, "tv") }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Cartão de uma série em andamento: capa à esquerda; à direita o nome, o próximo episódio
 * e, quando o total de episódios é conhecido, a barra de progresso com a contagem.
 */
@Composable
private fun CartaoContinuarAssistindo(
    item: ContinuarAssistindo,
    aoClicar: () -> Unit
) {
    Card(modifier = Modifier.width(larguraCartao)) {
        Row(
            modifier = Modifier
                .clickable(onClickLabel = "Abrir ${item.serie.nome}") { aoClicar() }
                .padding(10.dp)
        ) {
            AsyncImage(
                model = item.serie.caminhoCapa?.let { "https://image.tmdb.org/t/p/w185$it" },
                contentDescription = item.serie.nome,
                modifier = Modifier
                    .width(70.dp)
                    .height(105.dp)
                    .capaDeObra(),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = item.serie.nome,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Próximo episódio; sem ele, "Em dia" (se há total) ou um convite genérico
                Text(
                    text = when {
                        item.proximo != null ->
                            "Próximo: T${item.proximo.temporada} · E${item.proximo.numero}"
                        item.total > 0 -> "Em dia"
                        else -> "Toque para continuar"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                // Barra de progresso só quando o total é conhecido (detalhes guardados)
                if (item.total > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { item.progresso },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${item.assistidos} de ${item.total} episódios",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
