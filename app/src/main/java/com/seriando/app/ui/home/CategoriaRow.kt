package com.seriando.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.seriando.app.model.Categoria
import com.seriando.app.model.ResultadoBusca

// Largura de cada cartão dentro da fileira. O DestaqueCard ocupa a largura
// inteira do espaço que recebe, então fixamos o tamanho aqui, por fora.
private val larguraCartao = 110.dp

/**
 * Uma fileira horizontal de uma categoria da Home (RF44, RF45, RF46):
 * título, cartões com rolagem horizontal e, no fim, o cartão "Ver mais".
 *
 * É um componente "burro": não busca dados, só desenha o que a HomeScreen
 * entrega. A busca e o estado ficam na tela.
 *
 * @param categoria          categoria desenhada (usamos o nome como título).
 * @param itens              itens da fileira. null = ainda carregando;
 *                           lista vazia = falha ou nada para mostrar.
 * @param aoClicarEmItem     toque em um cartão: recebe o id e o tipo ("movie" ou "tv").
 * @param aoClicarEmVerMais  toque no cartão final: abre a lista completa da categoria.
 */
@Composable
fun CategoriaRow(
    categoria: Categoria,
    itens: List<ResultadoBusca>?,
    aoClicarEmItem: (Int, String) -> Unit,
    aoClicarEmVerMais: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {

        // Título da categoria, alinhado com o resto da Home (margem de 24dp)
        Text(
            text = categoria.nome,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        when {
            // Estado de carregamento: indicador centralizado com altura parecida com a da fileira
            itens == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Estado vazio/erro: mensagem simples no lugar da fileira
            itens.isEmpty() -> {
                Text(
                    text = "Não foi possível carregar esta categoria.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }

            // Estado normal: fileira com rolagem horizontal
            else -> {
                LazyRow(
                    // Margem nas pontas para o primeiro e o último cartão não colarem na borda
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // A chave combina tipo + id porque um filme e uma série
                    // podem ter o mesmo id na TMDB
                    items(itens, key = { "${it.tipoMidia}_${it.id}" }) { item ->
                        Box(modifier = Modifier.width(larguraCartao)) {
                            DestaqueCard(
                                resultado = item,
                                aoClicar = {
                                    // Mesma regra que a HomeScreen já usava
                                    val tipoMidia = if (item.ehFilme) "movie" else "tv"
                                    aoClicarEmItem(item.id, tipoMidia)
                                }
                            )
                        }
                    }

                    // Último item da fileira: cartão "Ver mais" (RF46)
                    item {
                        CartaoVerMais(aoClicar = aoClicarEmVerMais)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Cartão final da fileira. Tem a mesma proporção dos pôsteres (2:3) para
 * parecer mais um item da fila, e o mesmo padding de 4dp do DestaqueCard
 * para alinhar com eles.
 */
@Composable
private fun CartaoVerMais(aoClicar: () -> Unit) {
    Box(
        modifier = Modifier
            .width(larguraCartao)
            .padding(4.dp)
            .aspectRatio(2f / 3f)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { aoClicar() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Ver mais",
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center
        )
    }
}