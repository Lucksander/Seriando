package com.seriando.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seriando.app.model.Categoria
import com.seriando.app.model.ResultadoBusca
import com.seriando.app.ui.theme.capaDeObra

// Largura de cada cartão dentro da fileira. O DestaqueCard ocupa a largura
// inteira do espaço que recebe, então fixamos o tamanho aqui, por fora.
private val larguraCartao = 110.dp

// Quantos títulos entram no ranking das fileiras "em alta" (a lista completa fica no "Ver mais")
private const val TAMANHO_DO_RANKING = 10

/**
 * Uma fileira horizontal de uma categoria da Home (RF44, RF45, RF46, RF55, RF56):
 * título tocável, cartões com rolagem horizontal e, no fim, o cartão "Ver mais".
 *
 * É um componente "burro": não busca dados, só desenha o que a HomeScreen
 * entrega. A busca e o estado ficam na tela.
 *
 * @param categoria          categoria desenhada (usamos o nome como título).
 * @param itens              itens da fileira. null = ainda carregando;
 *                           lista vazia = falha ou nada para mostrar.
 * @param aoClicarEmItem     toque em um cartão: recebe o id e o tipo ("movie" ou "tv").
 * @param aoClicarEmVerMais  toque no título ou no cartão final: abre a lista completa
 *                           da categoria (mesmo destino nos dois).
 */
@Composable
fun CategoriaRow(
    categoria: Categoria,
    itens: List<ResultadoBusca>?,
    aoClicarEmItem: (Int, String) -> Unit,
    aoClicarEmVerMais: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {

        // Título tocável (RF55): a seta ao lado indica que dá para tocar.
        // O padding fica DENTRO do clickable para a área de toque incluir as margens.
        Row(
            modifier = Modifier
                .clickable { aoClicarEmVerMais() }
                .padding(horizontal = 24.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = categoria.nome,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Ver todos de ${categoria.nome}",
                modifier = Modifier.size(20.dp)
            )
        }

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
                    // Nas fileiras "em alta" é um ranking: só o Top 10, com a posição na capa (RF85)
                    val itensDaFileira = if (categoria.emAlta) itens.take(TAMANHO_DO_RANKING) else itens
                    itemsIndexed(itensDaFileira, key = { _, it -> "${it.tipoMidia}_${it.id}" }) { indice, item ->
                        Box(modifier = Modifier.width(larguraCartao)) {
                            DestaqueCard(
                                resultado = item,
                                aoClicar = {
                                    // Mesma regra que a HomeScreen já usava
                                    val tipoMidia = if (item.ehFilme) "movie" else "tv"
                                    aoClicarEmItem(item.id, tipoMidia)
                                }
                            )
                            // Número grande no canto da capa, só no ranking
                            if (categoria.emAlta) {
                                Text(
                                    text = "${indice + 1}",
                                    style = MaterialTheme.typography.displaySmall.copy(
                                        fontWeight = FontWeight.Black,
                                        // Sombra escura: o número fica legível sobre qualquer capa
                                        shadow = Shadow(color = Color.Black, blurRadius = 12f)
                                    ),
                                    color = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        // Sobe o suficiente para não cobrir o nome embaixo da capa
                                        .padding(start = 10.dp, bottom = 26.dp)
                                )
                            }
                        }
                    }

                    // Último item da fileira: cartão "Ver mais" (RF46, RF56)
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
 * Cartão final da fileira (RF56). Tem o tamanho das capas (mesma largura e proporção 2:3,
 * com o mesmo padding de 4dp do DestaqueCard): no centro, uma seta dentro de um círculo
 * e, embaixo dela, o texto "Ver mais".
 */
@Composable
private fun CartaoVerMais(
    aoClicar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(larguraCartao)
            .padding(4.dp)
            .aspectRatio(2f / 3f)
            .capaDeObra()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { aoClicar() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Círculo com a seta dentro
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null, // decorativo: o texto abaixo já diz o que é
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ver mais",
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}