package com.seriando.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
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
import com.seriando.app.ui.usuario.UsuarioScreen

/**
 * Estrutura principal do app: as três abas (Home, Minha Lista e Conta) com a barra
 * de navegação inferior, cada aba com ícone e texto.
 *
 * A aba selecionada fica no AbaCache para sobreviver à ida e volta entre telas
 * (por exemplo, abrir os Detalhes por cima e voltar).
 *
 * @param aoClicarEmPesquisar abre a tela de busca.
 * @param aoClicarEmItem      abre os Detalhes (id e tipo "movie"/"tv").
 * @param aoClicarEmVerMais   abre a lista completa de uma categoria (recebe o id dela).
 */
@Composable
fun MainScaffold(
    aoClicarEmPesquisar: () -> Unit,
    aoClicarEmItem: (Int, String) -> Unit,
    aoClicarEmVerMais: (String) -> Unit // recebe o id da categoria
) {
    var abaSelecionada by remember { mutableIntStateOf(AbaCache.abaSelecionada) }

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
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    label = { Text("Home") }
                )
                // Aba 1: Minha Lista (lista)
                NavigationBarItem(
                    selected = abaSelecionada == 1,
                    onClick = {
                        abaSelecionada = 1
                        AbaCache.abaSelecionada = 1
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text("Minha Lista") }
                )
                // Aba 2: Conta (boneco)
                NavigationBarItem(
                    selected = abaSelecionada == 2,
                    onClick = {
                        abaSelecionada = 2
                        AbaCache.abaSelecionada = 2
                    },
                    icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    label = { Text("Conta") }
                )
            }
        }
    ) { paddingInterno ->
        Box(modifier = Modifier.padding(paddingInterno)) {
            when (abaSelecionada) {
                0 -> HomeScreen(
                    aoClicarEmPesquisar = aoClicarEmPesquisar,
                    aoClicarEmItem = aoClicarEmItem,
                    aoClicarEmVerMais = aoClicarEmVerMais
                )
                1 -> MinhaListaScreen(aoClicarEmItem = aoClicarEmItem)
                2 -> UsuarioScreen()
            }
        }
    }
}