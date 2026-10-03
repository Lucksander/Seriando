package com.seriando.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.AsyncImage
import com.seriando.app.model.ResultadoBusca

// Cartão usado na grade de destaques da Home: ocupa a largura
// inteira da célula da grade em que está, em vez de um tamanho fixo.
@Composable
fun DestaqueCard(resultado: ResultadoBusca, aoClicar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable { aoClicar() }
    ) {
        AsyncImage(
            model = resultado.caminhoCapa?.let { "https://image.tmdb.org/t/p/w185$it" },
            contentDescription = resultado.nome,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f),
            contentScale = ContentScale.Crop
        )
        Text(
            text = resultado.nome,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}