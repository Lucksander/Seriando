package com.seriando.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.seriando.app.data.cache.AbaCache
import com.seriando.app.ui.home.HomeScreen
import com.seriando.app.ui.mylist.MinhaListaScreen

// Estrutura com barra de navegação inferior, alternando entre
// Home (destaques + pesquisa) e Minha Lista.
@Composable
fun MainScaffold(
    aoClicarEmPesquisar: () -> Unit,
    aoClicarEmItem: (Int, String) -> Unit
) {
    // Começa com a aba que estava selecionada da última vez,
    // em vez de sempre voltar para a Home (índice 0).
    var abaSelecionada by remember { mutableIntStateOf(AbaCache.abaSelecionada) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = abaSelecionada == 0,
                    onClick = {
                        abaSelecionada = 0
                        AbaCache.abaSelecionada = 0
                    },
                    icon = {},
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = abaSelecionada == 1,
                    onClick = {
                        abaSelecionada = 1
                        AbaCache.abaSelecionada = 1
                    },
                    icon = {},
                    label = { Text("Minha Lista") }
                )
            }
        }
    ) { paddingInterno ->
        // Passa o padding do Scaffold para dentro das telas, para o conteúdo
        // nunca ficar coberto pela bottomBar na parte de baixo da tela.
        Box(modifier = Modifier.padding(paddingInterno)) {
            when (abaSelecionada) {
                0 -> HomeScreen(
                    aoClicarEmPesquisar = aoClicarEmPesquisar,
                    aoClicarEmItem = aoClicarEmItem
                )
                1 -> MinhaListaScreen(aoClicarEmItem = aoClicarEmItem)
            }
        }
    }
}