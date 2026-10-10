package com.seriando.app.ui.home

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.seriando.app.data.repository.CatalogoRepository
import com.seriando.app.model.Categorias
import com.seriando.app.model.ResultadoBusca
import kotlinx.coroutines.launch

/**
 * Lista completa de uma categoria (RF46): grade de 3 colunas com paginação
 * manual pelo botão "Carregar mais".
 *
 * @param categoriaId    id recebido pela rota (ex.: "acao"); resolvido em Categorias.porId.
 * @param aoClicarEmItem abre os Detalhes (id e tipo "movie"/"tv").
 */
@Composable
fun CategoriaScreen(
    categoriaId: String,
    aoClicarEmItem: (Int, String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repositorio = remember { CatalogoRepository() }

    // Categoria da rota; null só se o id for inválido (não deve acontecer pelo app)
    val categoria = remember(categoriaId) { Categorias.porId(categoriaId) }

    // Estado da tela
    var itens by remember { mutableStateOf<List<ResultadoBusca>>(emptyList()) } // tudo já carregado
    var paginaAtual by remember { mutableIntStateOf(0) }       // última página carregada (0 = nenhuma)
    var totalPaginas by remember { mutableIntStateOf(0) }      // limite informado pela TMDB
    var carregando by remember { mutableStateOf(false) }       // evita toques duplos
    var falhouNoInicio by remember { mutableStateOf(false) }   // falha na 1ª página

    /**
     * Busca a página indicada e acrescenta à lista.
     * Trata erro com Log + Toast, sem derrubar o app.
     */
    fun carregarPagina(numero: Int) {
        if (categoria == null || carregando) return
        carregando = true
        scope.launch {
            try {
                val pagina = repositorio.buscarPagina(categoria, numero)
                // distinctBy: as páginas de filmes e séries podem repetir um título,
                // e a grade exige chaves únicas
                itens = (itens + pagina.itens).distinctBy { "${it.tipoMidia}_${it.id}" }
                paginaAtual = numero
                totalPaginas = pagina.totalPaginas
                falhouNoInicio = false
            } catch (e: Exception) {
                Log.e("CategoriaScreen", "Falha ao carregar página $numero de $categoriaId", e)
                Toast.makeText(context, "Não foi possível carregar.", Toast.LENGTH_SHORT).show()
                // Só marca falha "de tela cheia" se ainda não há nada para mostrar
                if (itens.isEmpty()) falhouNoInicio = true
            } finally {
                carregando = false
            }
        }
    }

    // Efeito: carrega a 1ª página ao abrir a tela
    LaunchedEffect(categoriaId) {
        carregarPagina(1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding() // evita o título ficar sob a barra de status
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = categoria?.nome ?: "Categoria",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        when {
            // Id de rota inválido
            categoria == null -> {
                Text(
                    text = "Categoria não encontrada.",
                    modifier = Modifier.padding(24.dp)
                )
            }

            // Primeira carga em andamento: indicador central
            itens.isEmpty() && !falhouNoInicio -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            // Primeira carga falhou: mensagem e botão para tentar de novo
            itens.isEmpty() && falhouNoInicio -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Não foi possível carregar esta categoria.")
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { carregarPagina(1) }) {
                        Text("Tentar novamente")
                    }
                }
            }

            // Estado normal: grade de pôsteres
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp)
                ) {
                    // Chave tipo + id: filme e série podem ter o mesmo id na TMDB
                    items(itens, key = { "${it.tipoMidia}_${it.id}" }) { item ->
                        DestaqueCard(
                            resultado = item,
                            aoClicar = {
                                val tipoMidia = if (item.ehFilme) "movie" else "tv"
                                aoClicarEmItem(item.id, tipoMidia)
                            }
                        )
                    }

                    // Rodapé ocupando a linha inteira (3 colunas)
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                // Carregando mais: indicador
                                carregando -> CircularProgressIndicator()
                                // Ainda há páginas: botão
                                paginaAtual < totalPaginas -> {
                                    Button(onClick = { carregarPagina(paginaAtual + 1) }) {
                                        Text("Carregar mais")
                                    }
                                }
                                // Chegou ao fim: nenhum botão
                                else -> Text(
                                    text = "Isso é tudo por aqui.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}