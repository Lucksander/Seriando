package com.seriando.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.seriando.app.model.Serie

// Cartão visual de uma série, usado na lista de resultados da busca.
// aoClicar é chamado quando o usuário toca no cartão, para navegar
// até a tela de detalhes daquela série.
@Composable
fun SerieCard(serie: Serie, aoClicar: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { aoClicar() } // torna o cartão inteiro clicável
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = serie.caminhoCapa?.let { "https://image.tmdb.org/t/p/w185$it" },
                contentDescription = serie.nome,
                modifier = Modifier
                    .width(90.dp)
                    .height(135.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = serie.nome,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = serie.dataEstreia?.take(4) ?: "Sem data",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = serie.sinopse.ifBlank { "Sem sinopse disponível." },
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}