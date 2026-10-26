package com.seriando.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seriando.app.model.ResultadoBusca

// Mesma largura de cartão das fileiras de categoria, para as capas ficarem alinhadas
private val larguraCartao = 110.dp

/**
 * Fileira "Recomendados para você" da Home (RF81): obras parecidas com as que o usuário
 * já terminou. Diferente da CategoriaRow, não tem "Ver mais" nem estado de carregamento:
 * quando não há recomendações, quem chama simplesmente não a desenha.
 *
 * É um componente "burro": só desenha o que a HomeScreen entrega.
 *
 * @param itens          recomendações já combinadas e filtradas por idade (RF50).
 * @param aoClicarEmItem toque em um cartão: recebe o id e o tipo ("movie" ou "tv").
 */
@Composable
fun FileiraDeRecomendacoes(
    itens: List<ResultadoBusca>,
    aoClicarEmItem: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Recomendados para você",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            // Mesma margem das outras fileiras
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Chave tipo + id: filme e série podem ter o mesmo id na TMDB
            items(itens, key = { "${it.tipoMidia}_${it.id}" }) { item ->
                Box(modifier = Modifier.width(larguraCartao)) {
                    DestaqueCard(
                        resultado = item,
                        aoClicar = {
                            val tipoMidia = if (item.ehFilme) "movie" else "tv"
                            aoClicarEmItem(item.id, tipoMidia)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
