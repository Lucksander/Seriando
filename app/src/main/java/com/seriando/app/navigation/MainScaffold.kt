package com.seriando.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.seriando.app.data.cache.AbaCache
import com.seriando.app.data.local.PreferenciasApp
import com.seriando.app.ui.descobrir.DescobrirScreen
import com.seriando.app.ui.home.HomeScreen
import com.seriando.app.ui.mylist.MinhaListaScreen
import com.seriando.app.ui.tour.LocalTour
import com.seriando.app.ui.tour.PassoDoTour
import com.seriando.app.ui.tour.TourOverlay
import com.seriando.app.ui.tour.alvoDoTour
import com.seriando.app.ui.tour.rememberTourState
import com.seriando.app.ui.usuario.UsuarioScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Estrutura principal do app: as quatro abas (Home, Descobrir, Minha Lista e Conta)
 * com a barra de navegação inferior, cada aba com ícone (estilo Outlined) e texto.
 *
 * A aba selecionada fica no AbaCache para sobreviver à ida e volta entre telas
 * (por exemplo, abrir os Detalhes por cima e voltar).
 *
 * Também hospeda o tour guiado da Home: o [TourOverlay] é desenhado por cima do Scaffold
 * inteiro (para escurecer até a barra inferior) e o estado do tour é entregue às telas
 * pelo [LocalTour]; cada tela marca seus elementos com Modifier.alvoDoTour.
 *
 * @param aoClicarEmPesquisar abre a tela de busca.
 * @param aoClicarEmItem      abre os Detalhes (id e tipo "movie"/"tv").
 * @param aoClicarEmVerMais   abre a lista completa de uma categoria (recebe o id dela).
 * @param aoAbrirTermos       abre os Termos de Uso (linha da aba Conta).
 * @param aoAbrirPrivacidade  abre a Política de Privacidade (linha da aba Conta).
 */
@Composable
fun MainScaffold(
    aoClicarEmPesquisar: () -> Unit,
    aoClicarEmItem: (Int, String) -> Unit,
    aoClicarEmVerMais: (String) -> Unit, // recebe o id da categoria
    // Chamado depois que o usuário sai da conta ou a exclui (RF53): o NavHost leva à tela de entrada
    aoSair: () -> Unit = {},
    // Abrem os textos legais a partir da aba Conta
    aoAbrirTermos: () -> Unit = {},
    aoAbrirPrivacidade: () -> Unit = {}
) {
    var abaSelecionada by remember { mutableIntStateOf(AbaCache.abaSelecionada) }

    // ---------- Tour guiado da Home ----------
    val contexto = LocalContext.current
    val escopo = rememberCoroutineScope()
    val preferencias = remember { PreferenciasApp(contexto) }

    // Já viu o tour? null = ainda lendo o DataStore; true/false = valor lido.
    // Lido uma vez ao abrir; depois disso, quem controla o início é tourJaIniciou.
    var tourHomeVisto by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(Unit) { tourHomeVisto = preferencias.tourHomeVisto.first() }

    // true depois que o tour começou nesta sessão. Impede que ele reinicie ao trocar de aba
    // e voltar para a Home, e sobrevive à rotação da tela (rememberSaveable).
    var tourJaIniciou by rememberSaveable { mutableStateOf(false) }

    // Os passos, na ordem em que aparecem. Os ids precisam ser iguais aos usados em
    // Modifier.alvoDoTour nas telas. Os três primeiros ficam na Home (a rolagem até cada
    // alvo é feita pela própria HomeScreen); os três últimos são as abas da barra inferior.
    // Passos cujo alvo não está na tela (carrossel sem destaques, por exemplo) são pulados.
    val passos = remember {
        listOf(
            PassoDoTour(
                alvoId = "busca",
                titulo = "Encontre qualquer título",
                texto = "Toque aqui para pesquisar séries e filmes. Suas pesquisas e as obras que você viu por último ficam guardadas."
            ),
            PassoDoTour(
                alvoId = "destaques",
                titulo = "Em alta agora",
                texto = "Toque em uma capa para ver os detalhes da série ou do filme."
            ),
            PassoDoTour(
                alvoId = "categoria",
                titulo = "Explore por gênero",
                texto = "Deslize para o lado em cada fileira. No fim dela, toque em Ver mais para abrir a lista completa."
            ),
            PassoDoTour(
                alvoId = "aba_descobrir",
                titulo = "Descobrir",
                texto = "Filtre por tipo, catálogo e gênero. Sem ideia do que assistir? Toque em Sortear."
            ),
            PassoDoTour(
                alvoId = "aba_minha_lista",
                titulo = "Minha Lista",
                texto = "Guarde o que quer ver, defina o status e marque os episódios assistidos. Funciona até sem internet."
            ),
            PassoDoTour(
                alvoId = "aba_conta",
                titulo = "Sua conta",
                texto = "Escolha o tema do app e leia os termos. Com uma conta, você também edita seu perfil e vê suas estatísticas."
            )
        )
    }
    val tour = rememberTourState(passos)

    // Inicia o tour na primeira vez: só na aba Home, só se ainda não foi visto e só depois
    // que o cabeçalho da Home apareceu na tela (o alvo "busca" já foi medido). A pequena
    // espera deixa o carrossel e as fileiras começarem a carregar antes do primeiro balão.
    // Se o usuário trocar de aba durante a espera, o efeito é cancelado e recomeça ao voltar.
    LaunchedEffect(tourHomeVisto, abaSelecionada) {
        if (tourHomeVisto == false && abaSelecionada == 0 && !tourJaIniciou) {
            snapshotFlow { tour.alvos["busca"] }
                .filter { it != null && !it.isEmpty }
                .first()
            delay(800)
            tourJaIniciou = true
            tour.iniciar()
        }
    }

    // O CompositionLocalProvider entrega o estado do tour a todas as telas abaixo;
    // o Box permite desenhar o overlay por cima do Scaffold, inclusive da barra inferior
    CompositionLocalProvider(LocalTour provides tour) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        // Aba 0: Home (casinha)
                        NavigationBarItem(
                            selected = abaSelecionada == 0,
                            onClick = {
                                abaSelecionada = 0
                                AbaCache.abaSelecionada = 0
                            },
                            // contentDescription nulo: o texto do rótulo já descreve a aba
                            icon = { Icon(Icons.Outlined.Home, contentDescription = null) },
                            label = { Text("Home") }
                        )
                        // Aba 1: Descobrir (bússola)
                        NavigationBarItem(
                            selected = abaSelecionada == 1,
                            onClick = {
                                abaSelecionada = 1
                                AbaCache.abaSelecionada = 1
                            },
                            icon = { Icon(Icons.Outlined.Explore, contentDescription = null) },
                            label = { Text("Descobrir") },
                            modifier = Modifier.alvoDoTour("aba_descobrir") // alvo do tour
                        )
                        // Aba 2: Minha Lista (marcador)
                        NavigationBarItem(
                            selected = abaSelecionada == 2,
                            onClick = {
                                abaSelecionada = 2
                                AbaCache.abaSelecionada = 2
                            },
                            icon = { Icon(Icons.Outlined.BookmarkBorder, contentDescription = null) },
                            label = { Text("Minha Lista") },
                            modifier = Modifier.alvoDoTour("aba_minha_lista") // alvo do tour
                        )
                        // Aba 3: Conta (boneco)
                        NavigationBarItem(
                            selected = abaSelecionada == 3,
                            onClick = {
                                abaSelecionada = 3
                                AbaCache.abaSelecionada = 3
                            },
                            icon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                            label = { Text("Conta") },
                            modifier = Modifier.alvoDoTour("aba_conta") // alvo do tour
                        )
                    }
                }
            ) { paddingInterno ->
                Box(modifier = Modifier.padding(paddingInterno)) {
                    when (abaSelecionada) {
                        0 -> HomeScreen(
                            aoClicarEmPesquisar = aoClicarEmPesquisar,
                            aoClicarEmItem = aoClicarEmItem,
                            aoClicarEmVerMais = aoClicarEmVerMais,
                            // Tocar no avatar da Home troca para a aba Conta (índice 3)
                            // e guarda a escolha no AbaCache, como o toque na barra inferior
                            aoClicarEmPerfil = {
                                abaSelecionada = 3
                                AbaCache.abaSelecionada = 3
                            }
                        )
                        1 -> DescobrirScreen(aoClicarEmItem = aoClicarEmItem)
                        2 -> MinhaListaScreen(aoClicarEmItem = aoClicarEmItem)
                        3 -> UsuarioScreen(
                            aoSair = aoSair,
                            aoAbrirTermos = aoAbrirTermos,
                            aoAbrirPrivacidade = aoAbrirPrivacidade,
                            // "Rever tour": apaga as duas flags (Home e Detalhes), libera o início
                            // do tour da Home, volta para essa aba e deixa o efeito de início
                            // rodar de novo. O tour dos Detalhes reaparece na próxima obra aberta.
                            aoReverTour = {
                                escopo.launch { preferencias.reiniciarTour() }
                                tourJaIniciou = false
                                tourHomeVisto = false
                                abaSelecionada = 0
                                AbaCache.abaSelecionada = 0
                            }
                        )
                    }
                }
            }

            // Camada do tour, por cima de tudo. Não desenha nada quando o tour está parado.
            // Ao terminar (concluído ou pulado), grava a flag, mas só se algum passo foi
            // realmente exibido; assim um tour que não achou nenhum alvo tenta de novo depois.
            TourOverlay(
                estado = tour,
                aoEncerrar = { viuAlgumPasso ->
                    if (viuAlgumPasso) {
                        tourHomeVisto = true
                        escopo.launch { preferencias.marcarTourHomeVisto() }
                    }
                }
            )
        }
    }
}
