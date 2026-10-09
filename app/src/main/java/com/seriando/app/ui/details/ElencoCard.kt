package com.seriando.app.ui.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.seriando.app.model.ElencoItem

// Cartão compacto de um ator/atriz na fileira de elenco.
// aoClicar abre a página da pessoa (RF40).
@Composable
fun ElencoCard(ator: ElencoItem, aoClicar: () -> Unit) {
    Column(
        modifier = Modifier
            .width(80.dp)
            .padding(end = 12.dp)
            .clickable { aoClicar() }
    ) {
        AsyncImage(
            model = ator.caminhoFoto?.let { "https://image.tmdb.org/t/p/w185$it" },
            contentDescription = ator.name,
            modifier = Modifier
                .width(80.dp)
                .aspectRatio(2f / 3f),
            contentScale = ContentScale.Crop
        )
        Text(
            text = ator.name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        ator.personagem?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}