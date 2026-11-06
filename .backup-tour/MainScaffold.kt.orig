package com.seriando.app.navigation

import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.seriando.app.data.cache.AbaCache
import com.seriando.app.ui.descobrir.DescobrirScreen
import com.seriando.app.ui.home.HomeScreen
import com.seriando.app.ui.mylist.MinhaListaScreen
import com.seriando.app.ui.usuario.UsuarioScreen

/**
 * Estrutura principal do app: as quatro abas (Home, Descobrir, Minha Lista e Conta)
 * com a barra de navegação inferior, cada aba com ícone (estilo Outlined) e texto.
 *
 * A aba selecionada fica no AbaCache para sobreviver à ida e volta entre telas
 * (por exemplo, abrir os Detalhes por cima e voltar).
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
                    label = { Text("Descobrir") }
                )
                // Aba 2: Minha Lista (marcador)
                NavigationBarItem(
                    selected = abaSelecionada == 2,
                    onClick = {
                        abaSelecionada = 2
                        AbaCache.abaSelecionada = 2
                    },
                    icon = { Icon(Icons.Outlined.BookmarkBorder, contentDescription = null) },
                    label = { Text("Minha Lista") }
                )
                // Aba 3: Conta (boneco)
                NavigationBarItem(
                    selected = abaSelecionada == 3,
                    onClick = {
                        abaSelecionada = 3
                        AbaCache.abaSelecionada = 3
                    },
                    icon = { Icon(Icons.Outlined.Person, contentDescription = null) },
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
                    aoAbrirPrivacidade = aoAbrirPrivacidade
                )
            }
        }
    }
}