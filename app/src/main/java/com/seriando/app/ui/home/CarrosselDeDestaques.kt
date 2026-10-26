package com.seriando.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.seriando.app.model.ResultadoBusca
import kotlinx.coroutines.delay

// Tempo que cada destaque fica na tela antes de passar sozinho para o próximo
private const val INTERVALO_AUTOMATICO_MS = 5_000L

/**
 * Carrossel de destaques no topo da Home (RF84): cartões grandes com a imagem horizontal
 * (backdrop) de obras em alta, o tipo, o nome e um trecho da sinopse. Passa sozinho a cada
 * 5 segundos e também pode ser arrastado; os pontinhos embaixo mostram a posição.
 * Tocar em um cartão abre os detalhes da obra.
 *
 * É um componente "burro": só desenha o que a HomeScreen entrega. Sem itens, quem chama
 * simplesmente não o desenha.
 *
 * @param itens          obras do carrossel (ver DestaquesDaHome.escolher); precisam ter caminhoFundo.
 * @param aoClicarEmItem toque em um cartão: recebe o id e o tipo ("movie" ou "tv").
 */
@Composable
fun CarrosselDeDestaques(
    itens: List<ResultadoBusca>,
    aoClicarEmItem: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Estado do pager: guarda a página atual; o lambda informa quantas páginas existem
    val estado = rememberPagerState { itens.size }

    // true enquanto o usuário está com o dedo arrastando: a passagem automática espera
    val arrastando by estado.interactionSource.collectIsDraggedAsState()

    // Passagem automática. O efeito recomeça quando o usuário solta o dedo (arrastando muda)
    // ou quando a quantidade de itens muda, e é cancelado sozinho ao sair da tela.
    LaunchedEffect(itens.size, arrastando) {
        if (itens.size > 1 && !arrastando) {
            while (true) {
                delay(INTERVALO_AUTOMATICO_MS)
                estado.animateScrollToPage((estado.currentPage + 1) % itens.size)
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = estado,
            // Margem nas pontas: deixa um pedaço do próximo cartão aparecendo
            contentPadding = PaddingValues(horizontal = 20.dp),
            pageSpacing = 8.dp,
            // Chave tipo + id: filme e série podem ter o mesmo id na TMDB
            key = { "${itens[it].tipoMidia}_${itens[it].id}" }
        ) { pagina ->
            CartaoDeDestaque(
                item = itens[pagina],
                aoClicar = {
                    val tipoMidia = if (itens[pagina].ehFilme) "movie" else "tv"
                    aoClicarEmItem(itens[pagina].id, tipoMidia)
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Pontinhos de posição: o da página atual é destacado
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(itens.size) { indice ->
                val corDoPonto = if (indice == estado.currentPage) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(corDoPonto)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

/**
 * Um cartão do carrossel: imagem de fundo, degradê escuro embaixo (para o texto branco ficar
 * legível sobre qualquer imagem) e, por cima, o tipo, o nome e a sinopse resumida.
 */
@Composable
private fun CartaoDeDestaque(
    item: ResultadoBusca,
    aoClicar: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClickLabel = "Abrir ${item.nome}") { aoClicar() }
    ) {
        // w780 é a largura média de backdrop da TMDB: nítida no celular sem pesar
        AsyncImage(
            model = item.caminhoFundo?.let { "https://image.tmdb.org/t/p/w780$it" },
            contentDescription = item.nome,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Degradê: transparente no topo e escuro na base, onde fica o texto
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                        startY = 120f
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = if (item.ehFilme) "FILME EM ALTA" else "SÉRIE EM ALTA",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = item.nome,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            // Sinopse só se existir (algumas obras vêm sem texto em português)
            if (item.sinopse.isNotBlank()) {
                Text(
                    text = item.sinopse,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
