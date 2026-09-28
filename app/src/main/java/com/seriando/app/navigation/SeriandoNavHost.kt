package com.seriando.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.seriando.app.ui.details.DetailsScreen
import com.seriando.app.ui.home.HomeScreen
import com.seriando.app.ui.search.SearchScreen

// Componente central de navegação do app.
@Composable
fun SeriandoNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Rotas.HOME
    ) {
        composable(Rotas.HOME) {
            HomeScreen(
                aoClicarEmPesquisar = { navController.navigate(Rotas.BUSCA) }
            )
        }

        composable(Rotas.BUSCA) {
            SearchScreen(
                // Ao clicar numa série, navega para a rota de detalhes
                // já com o ID preenchido (ex: "detalhes/1399").
                aoClicarEmSerie = { serieId ->
                    navController.navigate(Rotas.detalhesComId(serieId))
                }
            )
        }

        // Declara a rota de detalhes, avisando que ela espera
        // um argumento chamado "serieId" do tipo inteiro.
        composable(
            route = Rotas.DETALHES,
            arguments = listOf(navArgument("serieId") { type = NavType.IntType })
        ) { backStackEntry ->
            // Lê o serieId que veio na navegação e repassa para a tela.
            val serieId = backStackEntry.arguments?.getInt("serieId") ?: 0
            DetailsScreen(serieId = serieId)
        }
    }
}