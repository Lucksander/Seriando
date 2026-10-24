package com.seriando.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.seriando.app.data.auth.AuthRepository
import com.seriando.app.data.local.PreferenciasApp
import com.seriando.app.ui.details.DetailsScreen
import com.seriando.app.ui.home.CategoriaScreen
import com.seriando.app.ui.introducao.IntroducaoScreen
import com.seriando.app.ui.legal.POLITICA_DE_PRIVACIDADE
import com.seriando.app.ui.legal.TERMOS_DE_USO
import com.seriando.app.ui.legal.TextoLegalScreen
import com.seriando.app.ui.login.EntradaScreen
import com.seriando.app.ui.pessoa.PessoaScreen
import com.seriando.app.ui.search.SearchScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// Grafo de navegação do app: declara as telas e como elas se conectam.
@Composable
fun SeriandoNavHost(
    navController: NavHostController = rememberNavController()
) {
    val contexto = LocalContext.current
    val escopo = rememberCoroutineScope()
    val preferencias = remember { PreferenciasApp(contexto) }
    val authRepository = remember { AuthRepository() }

    // Lê UMA vez, ao abrir o app, se a introdução já foi vista (RF52).
    // null = ainda lendo o DataStore (leva alguns milissegundos).
    // Usar first() (e não observar) evita que o grafo troque de destino inicial quando
    // a flag for gravada ao concluir a introdução.
    val introducaoVista by produceState<Boolean?>(initialValue = null) {
        value = preferencias.introducaoVista.first()
    }

    // Enquanto a flag não chega, mostra só o fundo (a Surface da MainActivity) e não monta o grafo
    val vista = introducaoVista
    if (vista == null) {
        Box(modifier = Modifier.fillMaxSize())
        return
    }

    // Destino inicial, calculado uma vez (RF52 e RF53):
    // - logado: vai direto à Home;
    // - sem sessão e introdução nunca vista: começa pela introdução;
    // - sem sessão e introdução já vista: tela de entrada (login ou visitante).
    val destinoInicial = remember {
        when {
            authRepository.usuarioAtual() != null -> Rotas.HOME
            !vista -> Rotas.INTRODUCAO
            else -> Rotas.ENTRADA
        }
    }

    NavHost(
        navController = navController,
        startDestination = destinoInicial
    ) {
        // Introdução (RF52): ao pular ou concluir, grava a flag e segue para a tela de entrada.
        // popUpTo remove a introdução da pilha, para o botão Voltar não retornar a ela.
        composable(Rotas.INTRODUCAO) {
            IntroducaoScreen(
                // Links da frase "Ao continuar, você concorda..." abrem os textos legais
                aoAbrirTermos = { navController.navigate(Rotas.TERMOS) },
                aoAbrirPrivacidade = { navController.navigate(Rotas.PRIVACIDADE) },
                aoConcluir = {
                    escopo.launch { preferencias.marcarIntroducaoVista() }
                    navController.navigate(Rotas.ENTRADA) {
                        popUpTo(Rotas.INTRODUCAO) { inclusive = true }
                    }
                }
            )
        }

        // Entrada (RF53): login com Google ou visitante. Nos dois casos o destino é a Home,
        // e a entrada sai da pilha para o botão Voltar não retornar a ela.
        composable(Rotas.ENTRADA) {
            EntradaScreen(
                aoAbrirTermos = { navController.navigate(Rotas.TERMOS) },
                aoAbrirPrivacidade = { navController.navigate(Rotas.PRIVACIDADE) },
                aoEntrarComSucesso = {
                    navController.navigate(Rotas.HOME) {
                        popUpTo(Rotas.ENTRADA) { inclusive = true }
                    }
                },
                aoContinuarComoVisitante = {
                    navController.navigate(Rotas.HOME) {
                        popUpTo(Rotas.ENTRADA) { inclusive = true }
                    }
                }
            )
        }

        composable(Rotas.HOME) {
            MainScaffold(
                // Linhas "Termos de Uso" e "Política de Privacidade" da aba Conta
                aoAbrirTermos = { navController.navigate(Rotas.TERMOS) },
                aoAbrirPrivacidade = { navController.navigate(Rotas.PRIVACIDADE) },
                aoClicarEmPesquisar = { navController.navigate(Rotas.BUSCA) },
                aoClicarEmItem = { id, tipoMidia ->
                    navController.navigate(Rotas.detalhesComId(tipoMidia, id))
                },
                // "Ver mais" de uma fileira da Home abre a lista completa da categoria (RF46)
                aoClicarEmVerMais = { categoriaId ->
                    navController.navigate(Rotas.categoriaComId(categoriaId))
                },
                // Saiu da conta ou excluiu (RF53): vai para a tela de entrada e limpa a pilha
                // inteira (inclusive Detalhes abertos), para o botão Voltar não reabrir telas
                // de um usuário que já saiu.
                aoSair = {
                    navController.navigate(Rotas.ENTRADA) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }

        // Termos de Uso e Política de Privacidade: o texto está no código, então abrem offline
        // e para visitante. O botão Voltar da barra superior desempilha a tela.
        composable(Rotas.TERMOS) {
            TextoLegalScreen(
                documento = TERMOS_DE_USO,
                aoVoltar = { navController.popBackStack() }
            )
        }

        composable(Rotas.PRIVACIDADE) {
            TextoLegalScreen(
                documento = POLITICA_DE_PRIVACIDADE,
                aoVoltar = { navController.popBackStack() }
            )
        }

        composable(Rotas.BUSCA) {
            SearchScreen(
                aoClicarEmItem = { id, tipoMidia ->
                    navController.navigate(Rotas.detalhesComId(tipoMidia, id))
                }
            )
        }

        // Lista completa de uma categoria da Home (RF46).
        // O id da categoria (ex.: "acao") chega pela rota e é repassado à tela.
        composable(
            route = Rotas.CATEGORIA,
            arguments = listOf(navArgument("categoriaId") { type = NavType.StringType })
        ) { backStackEntry ->
            // Se o argumento vier ausente, usa "" e a tela mostra "Categoria não encontrada."
            val categoriaId = backStackEntry.arguments?.getString("categoriaId") ?: ""
            CategoriaScreen(
                categoriaId = categoriaId,
                // Tocar num pôster abre os Detalhes, igual às outras telas
                aoClicarEmItem = { id, tipoMidia ->
                    navController.navigate(Rotas.detalhesComId(tipoMidia, id))
                }
            )
        }

        composable(
            route = Rotas.DETALHES,
            arguments = listOf(
                navArgument("tipoMidia") { type = NavType.StringType },
                navArgument("serieId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val tipoMidia = backStackEntry.arguments?.getString("tipoMidia") ?: "tv"
            val serieId = backStackEntry.arguments?.getInt("serieId") ?: 0
            DetailsScreen(
                serieId = serieId,
                tipoMidia = tipoMidia,
                // Tocar num ator do elenco abre a página dele (RF40).
                aoClicarEmAtor = { pessoaId ->
                    navController.navigate(Rotas.pessoaComId(pessoaId))
                },
                // Botão "Voltar" do aviso de obra bloqueada por idade (RF51)
                aoVoltar = { navController.popBackStack() }
            )
        }

        // Página do ator/atriz. Tocar num título da filmografia volta a abrir
        // a tela de Detalhes, agora da obra escolhida (RF43).
        composable(
            route = Rotas.PESSOA,
            arguments = listOf(navArgument("pessoaId") { type = NavType.IntType })
        ) { backStackEntry ->
            val pessoaId = backStackEntry.arguments?.getInt("pessoaId") ?: 0
            PessoaScreen(
                pessoaId = pessoaId,
                aoClicarEmItem = { id, tipoMidia ->
                    navController.navigate(Rotas.detalhesComId(tipoMidia, id))
                }
            )
        }
    }
}