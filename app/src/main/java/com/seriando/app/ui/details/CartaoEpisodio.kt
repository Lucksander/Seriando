package com.seriando.app.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.seriando.app.model.EpisodioResumo
import com.seriando.app.ui.theme.capaDeObra

/**
 * Cartão de um episódio na fileira horizontal da tela de Detalhes (RF47, RF57):
 * thumbnail grande com bordas arredondadas e, embaixo, a caixa de "assistido"
 * ao lado do número e do nome.
 *
 * @param episodio  dados do episódio (número, nome e caminho da imagem).
 * @param assistido true se o usuário já marcou este episódio.
 * @param aoMarcar  chamado ao tocar na caixa, com o novo valor (true = marcado).
 */
@Composable
fun CartaoEpisodio(
    episodio: EpisodioResumo,
    assistido: Boolean,
    aoMarcar: (Boolean) -> Unit
) {
    Column(modifier = Modifier.width(220.dp)) {
        // Nem todo episódio tem imagem na TMDB: o fundo cinza evita um espaço vazio
        AsyncImage(
            model = episodio.caminhoThumbnail?.let { "https://image.tmdb.org/t/p/w300$it" },
            contentDescription = episodio.nome,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .capaDeObra()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = assistido, onCheckedChange = aoMarcar)
            Text(
                text = "${episodio.numero}. ${episodio.nome}",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f) // o texto ocupa o espaço restante, sem estourar a largura
            )
        }
    }
}