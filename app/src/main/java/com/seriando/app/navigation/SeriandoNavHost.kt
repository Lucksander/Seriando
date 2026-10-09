package com.seriando.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.seriando.app.ui.details.DetailsScreen
import com.seriando.app.ui.pessoa.PessoaScreen
import com.seriando.app.ui.search.SearchScreen

// Grafo de navegação do app: declara as telas e como elas se conectam.
@Composable
fun SeriandoNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Rotas.HOME
    ) {
        composable(Rotas.HOME) {
            MainScaffold(
                aoClicarEmPesquisar = { navController.navigate(Rotas.BUSCA) },
                aoClicarEmItem = { id, tipoMidia ->
                    navController.navigate(Rotas.detalhesComId(tipoMidia, id))
                }
            )
        }

        composable(Rotas.BUSCA) {
            SearchScreen(
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
                }
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