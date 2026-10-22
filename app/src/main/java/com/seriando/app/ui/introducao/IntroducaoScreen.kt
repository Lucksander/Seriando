package com.seriando.app.ui.introducao

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seriando.app.R
import com.seriando.app.ui.comum.LogoSeriando
import kotlinx.coroutines.launch

// Fonte Poppins já presente no projeto (res/font/poppins_semibold.ttf), usada nos títulos
private val Poppins = FontFamily(Font(R.font.poppins_semibold, FontWeight.SemiBold))

// Conteúdo de uma página da introdução. icone == null significa "mostrar o logotipo do Seriando".
private data class PaginaIntroducao(
    val titulo: String,
    val subtitulo: String,
    val icone: ImageVector?
)

// As 3 páginas, com os textos definidos na especificação do Bloco L (RF52)
private val paginas = listOf(
    PaginaIntroducao(
        titulo = "O seu cantinho das séries",
        subtitulo = "Organize o que você já assistiu, controle suas temporadas e acompanhe tudo no seu próprio ritmo, sem complicação.",
        icone = null
    ),
    PaginaIntroducao(
        titulo = "Saiba onde está passando",
        subtitulo = "Descubra instantaneamente em qual serviço de streaming (Netflix, Prime, Max, Disney+ e mais) sua série ou filme favorito está disponível no Brasil.",
        icone = Icons.Outlined.Tv
    ),
    PaginaIntroducao(
        titulo = "Compartilhe suas impressões",
        subtitulo = "Avalie, escreva opiniões sem spoilers, registre como se sentiu e veja o que a comunidade está achando das produções.",
        icone = Icons.Outlined.ChatBubbleOutline
    )
)

/**
 * Introdução do app (RF52): 3 páginas deslizáveis com indicador de pontos.
 *
 * @param aoConcluir chamada ao tocar em "Pular" (em qualquer página) ou em "Começar" (última).
 *   Quem a recebe (o SeriandoNavHost) grava a flag "introducao_vista" e navega; a tela não
 *   conhece navegação nem DataStore.
 */
@Composable
fun IntroducaoScreen(aoConcluir: () -> Unit) {
    // Estado do pager: guarda a página atual; o lambda informa quantas páginas existem
    val estadoPager = rememberPagerState { paginas.size }
    // Escopo para animar a rolagem do pager ao tocar em "Avançar"
    val escopo = rememberCoroutineScope()
    val ehUltimaPagina = estadoPager.currentPage == paginas.lastIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Conteúdo fora das barras do sistema (RNF14)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Topo: "Pular" no canto superior direito
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = aoConcluir) { Text("Pular") }
        }

        // Centro: páginas deslizáveis (ocupam todo o espaço que sobra)
        HorizontalPager(
            state = estadoPager,
            modifier = Modifier.weight(1f)
        ) { indice ->
            ConteudoDaPagina(paginas[indice])
        }

        // Indicador de página (pontos)
        IndicadorDePaginas(
            total = paginas.size,
            atual = estadoPager.currentPage,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 16.dp)
        )

        // Rodapé: "Avançar" nas duas primeiras, "Começar" na última
        Button(
            onClick = {
                if (ehUltimaPagina) {
                    aoConcluir()
                } else {
                    // Desliza para a próxima página com animação
                    escopo.launch { estadoPager.animateScrollToPage(estadoPager.currentPage + 1) }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .height(52.dp)
        ) {
            Text(if (ehUltimaPagina) "Começar" else "Avançar")
        }
    }
}

// Uma página: ícone (ou logotipo) com brilho suave atrás, título e subtítulo centralizados.
@Composable
private fun ConteudoDaPagina(pagina: PaginaIntroducao) {
    val corPrincipal = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Círculo com degradê radial na cor da marca, dando o "brilho" atrás do ícone
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(corPrincipal.copy(alpha = 0.30f), Color.Transparent)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (pagina.icone == null) {
                // Página 1: logotipo oficial
                LogoSeriando(alturaIcone = 56.dp, tamanhoTexto = 38.sp)
            } else {
                Icon(
                    imageVector = pagina.icone,
                    contentDescription = null, // decorativo: o título já descreve a página
                    modifier = Modifier.size(96.dp),
                    tint = corPrincipal
                )
            }
        }

        Text(
            text = pagina.titulo,
            modifier = Modifier.padding(top = 32.dp),
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = Poppins,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        Text(
            text = pagina.subtitulo,
            modifier = Modifier.padding(top = 12.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// Pontos que indicam em qual página o usuário está; o da página atual usa a cor da marca.
@Composable
private fun IndicadorDePaginas(total: Int, atual: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(total) { indice ->
            val cor = if (indice == atual) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
            }
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(cor)
            )
        }
    }
}