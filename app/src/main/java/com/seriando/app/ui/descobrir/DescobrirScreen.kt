package com.seriando.app.ui.descobrir

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.seriando.app.data.cache.DescobrirCache
import com.seriando.app.data.repository.CatalogoRepository
import com.seriando.app.data.sync.MonitorConexao
import com.seriando.app.model.CatalogoDescoberta
import com.seriando.app.model.Genero
import com.seriando.app.model.ProvedorStreaming
import com.seriando.app.model.ResultadoBusca
import com.seriando.app.model.TipoDescoberta
import com.seriando.app.ui.comum.MensagemSemConexao
import com.seriando.app.ui.theme.capaDeObra
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Aba Descobrir (RF63, RF64, RF65): três menus (Tipo, Catálogo e Gênero), uma grade de
 * capas com 4 colunas, que carrega a próxima página sozinha ao rolar até perto do fim,
 * e o botão "Sortear" ("O que assistir hoje?").
 *
 * Filtros e resultados ficam no DescobrirCache, para sobreviver à ida e volta aos Detalhes.
 *
 * @param aoClicarEmItem abre os Detalhes (id e tipo "movie"/"tv").
 */
@Composable
fun DescobrirScreen(
    aoClicarEmItem: (Int, String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repositorio = remember { CatalogoRepository() }

    // Filtros escolhidos, restaurados do cache ao voltar para a aba
    var tipo by remember { mutableStateOf(DescobrirCache.tipo) }
    var catalogo by remember { mutableStateOf<CatalogoDescoberta>(DescobrirCache.catalogo) }
    var generoId by remember { mutableStateOf<Int?>(DescobrirCache.generoId) }

    // Resultados e paginação, também restaurados do cache
    var itens by remember { mutableStateOf<List<ResultadoBusca>>(DescobrirCache.itens) }
    var paginaAtual by remember { mutableIntStateOf(DescobrirCache.paginaAtual) }   // última carregada
    var totalPaginas by remember { mutableIntStateOf(DescobrirCache.totalPaginas) }
    var paginaPedida by remember { mutableIntStateOf(maxOf(DescobrirCache.paginaAtual, 1)) } // a que se quer ver

    // Estado da carga
    var carregando by remember { mutableStateOf(false) }
    var falhou by remember { mutableStateOf(false) }
    var tentativa by remember { mutableIntStateOf(0) } // sobe 1 a cada "tentar de novo", para refazer a carga

    // Obra sorteada (RF65): enquanto não for nula, o diálogo do sorteio fica aberto.
    // Não vai para o cache: ao voltar para a aba, o diálogo não reabre sozinho.
    var sorteado by remember { mutableStateOf<ResultadoBusca?>(null) }

    // Opções dos menus que vêm da TMDB (gêneros e streamings do tipo escolhido)
    var generos by remember { mutableStateOf<List<Genero>>(emptyList()) }
    var provedores by remember { mutableStateOf<List<ProvedorStreaming>>(emptyList()) }

    // Posição da rolagem, restaurada do cache
    val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = DescobrirCache.indicePrimeiroItem,
        initialFirstVisibleItemScrollOffset = DescobrirCache.deslocamento
    )

    // Ao sair da tela (abrir Detalhes, trocar de aba), grava tudo no cache
    DisposableEffect(Unit) {
        onDispose {
            DescobrirCache.tipo = tipo
            DescobrirCache.catalogo = catalogo
            DescobrirCache.generoId = generoId
            DescobrirCache.itens = itens
            DescobrirCache.paginaAtual = paginaAtual
            DescobrirCache.totalPaginas = totalPaginas
            DescobrirCache.indicePrimeiroItem = gridState.firstVisibleItemIndex
            DescobrirCache.deslocamento = gridState.firstVisibleItemScrollOffset
        }
    }

    // Quando o app volta a ter internet depois de uma falha, tenta de novo sozinho
    val online by MonitorConexao.online.collectAsState()
    LaunchedEffect(online) {
        if (online && falhou) tentativa++
    }

    // Carrega as listas de gêneros e de streamings sempre que o tipo muda.
    // Se falhar, os menus ficam só com as opções fixas (a grade já avisa da falta de internet).
    LaunchedEffect(tipo) {
        try {
            generos = repositorio.buscarGeneros(tipo)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("DescobrirScreen", "Falha ao carregar gêneros", e)
        }
        try {
            provedores = repositorio.buscarProvedores(tipo)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("DescobrirScreen", "Falha ao carregar streamings", e)
        }
    }

    // Carga da grade. Refaz quando mudam os filtros, a página pedida ou a tentativa;
    // se uma carga estiver em andamento, o Compose a cancela antes de começar a nova.
    LaunchedEffect(tipo, catalogo, generoId, paginaPedida, tentativa) {
        // Já temos essa página (caso da volta do cache): nada a buscar
        if (itens.isNotEmpty() && paginaPedida == paginaAtual) return@LaunchedEffect

        carregando = true
        falhou = false
        try {
            val pagina = repositorio.descobrir(tipo, catalogo, generoId, paginaPedida)
            // distinctBy: a grade exige chaves únicas, e a TMDB pode repetir um título entre páginas
            itens = if (paginaPedida == 1) {
                pagina.itens
            } else {
                (itens + pagina.itens).distinctBy { "${it.tipoMidia}_${it.id}" }
            }
            paginaAtual = paginaPedida
            totalPaginas = pagina.totalPaginas
            carregando = false
        } catch (e: CancellationException) {
            // Cancelada por uma mudança de filtro: a nova carga assume o estado
            throw e
        } catch (e: Exception) {
            Log.e("DescobrirScreen", "Falha ao carregar página $paginaPedida", e)
            // Com a grade vazia quem avisa é a mensagem de tela cheia; com itens na tela, um Toast
            if (itens.isNotEmpty()) {
                Toast.makeText(context, "Não foi possível carregar mais.", Toast.LENGTH_SHORT).show()
            }
            falhou = true
            carregando = false
        }
    }

    // True quando o usuário rolou até perto do fim da grade (últimos 12 itens)
    val pertoDoFim by remember {
        derivedStateOf {
            val info = gridState.layoutInfo
            val ultimo = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            info.totalItemsCount > 0 && ultimo >= info.totalItemsCount - 12
        }
    }

    // Rolagem infinita: perto do fim, sem carga em andamento e com mais páginas, pede a próxima.
    // Só avança quando a página pedida já chegou (paginaPedida == paginaAtual).
    LaunchedEffect(pertoDoFim, carregando, falhou, paginaAtual, totalPaginas) {
        if (pertoDoFim && !carregando && !falhou &&
            paginaAtual in 1 until totalPaginas && paginaPedida == paginaAtual
        ) {
            paginaPedida = paginaAtual + 1
        }
    }

    /**
     * Aplica uma nova combinação de filtros: esvazia a grade, volta para a página 1
     * e rola para o topo. A carga em si é disparada pelo LaunchedEffect acima.
     */
    fun aplicarFiltros(novoTipo: TipoDescoberta, novoCatalogo: CatalogoDescoberta, novoGenero: Int?) {
        tipo = novoTipo
        catalogo = novoCatalogo
        generoId = novoGenero
        itens = emptyList()
        paginaAtual = 0
        totalPaginas = 0
        paginaPedida = 1
        falhou = false
        scope.launch { gridState.scrollToItem(0) }
    }

    /**
     * Sorteia uma obra entre as que já estão na grade (RF65), sem repetir a que acabou
     * de sair. Se a grade só tem essa obra, avisa em vez de sortear a mesma de novo.
     *
     * @param excluir obra a evitar (a sorteada anteriormente), ou null no primeiro sorteio.
     */
    fun sortear(excluir: ResultadoBusca?) {
        // Dentro de uma mesma grade o tipo é sempre o mesmo, então comparar o id basta
        val candidatos = itens.filter { it.id != excluir?.id }
        if (candidatos.isEmpty()) {
            Toast.makeText(context, "Não há outro título para sortear.", Toast.LENGTH_SHORT).show()
            return
        }
        sorteado = candidatos.random()
    }

    // Opções dos menus: o catálogo tem Popular, Novos e depois os streamings
    val opcoesCatalogo: List<CatalogoDescoberta> =
        listOf(CatalogoDescoberta.Popular, CatalogoDescoberta.Novos) +
                provedores.map { CatalogoDescoberta.Streaming(it) }
    val rotuloGenero = generos.firstOrNull { it.id == generoId }?.nome ?: "Todos"

    // O Box permite que o botão "Sortear" flutue por cima da grade
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Linha dos três menus, cada um ocupando um terço da largura
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Menu 1: Tipo. Trocar o tipo zera o gênero, porque os ids de filme e de série diferem
                MenuFiltro(
                    rotulo = tipo.rotulo,
                    opcoes = TipoDescoberta.entries.map { it.rotulo },
                    aoEscolher = { posicao ->
                        val escolhido = TipoDescoberta.entries[posicao]
                        if (escolhido != tipo) aplicarFiltros(escolhido, catalogo, null)
                    },
                    modifier = Modifier.weight(1f)
                )
                // Menu 2: Catálogo (Popular, Novos ou um streaming)
                MenuFiltro(
                    rotulo = catalogo.rotulo,
                    opcoes = opcoesCatalogo.map { it.rotulo },
                    aoEscolher = { posicao ->
                        val escolhido = opcoesCatalogo[posicao]
                        if (escolhido != catalogo) aplicarFiltros(tipo, escolhido, generoId)
                    },
                    modifier = Modifier.weight(1f)
                )
                // Menu 3: Gênero ("Todos" mais a lista do tipo escolhido)
                MenuFiltro(
                    rotulo = rotuloGenero,
                    opcoes = listOf("Todos") + generos.map { it.nome },
                    aoEscolher = { posicao ->
                        val escolhido = if (posicao == 0) null else generos[posicao - 1].id
                        if (escolhido != generoId) aplicarFiltros(tipo, catalogo, escolhido)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            when {
                // Falha sem nada para mostrar: mensagem de tela cheia com "Tentar de novo"
                itens.isEmpty() && falhou -> {
                    MensagemSemConexao(
                        texto = "Não foi possível carregar os títulos.",
                        aoTentarDeNovo = { tentativa++ },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // A carga terminou, mas os filtros não devolveram nenhuma obra
                itens.isEmpty() && paginaAtual > 0 && !carregando -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Nenhum título encontrado com esses filtros.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Primeira carga (ou troca de filtro) em andamento
                itens.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                // Estado normal: grade de capas com 4 colunas
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        state = gridState,
                        modifier = Modifier.fillMaxSize(),
                        // Folga maior embaixo: o botão "Sortear" não cobre a última linha
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Chave tipo + id: filme e série podem ter o mesmo id na TMDB
                        items(itens, key = { "${it.tipoMidia}_${it.id}" }) { item ->
                            CapaDaGrade(
                                resultado = item,
                                aoClicar = { aoClicarEmItem(item.id, tipo.valorTmdb) }
                            )
                        }

                        // Rodapé ocupando a linha inteira (4 colunas)
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                when {
                                    // Carregando a próxima página
                                    carregando -> CircularProgressIndicator()
                                    // Falhou ao carregar mais: botão para tentar de novo
                                    falhou -> Button(onClick = { tentativa++ }) { Text("Tentar de novo") }
                                    // Chegou ao fim da lista
                                    paginaAtual >= totalPaginas -> Text(
                                        text = "Isso é tudo por aqui.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                // Atribuição obrigatória dos dados de streaming (RNF09)
                                Text(
                                    text = "Dados de streaming: JustWatch",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Botão "Sortear" (RF65): só aparece quando há capas para sortear
        if (itens.isNotEmpty()) {
            ExtendedFloatingActionButton(
                onClick = { sortear(excluir = null) },
                icon = { Icon(Icons.Filled.Shuffle, contentDescription = null) },
                text = { Text("Sortear") },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }

    // Diálogo do sorteio: aberto enquanto houver uma obra sorteada
    sorteado?.let { obra ->
        DialogoSorteio(
            obra = obra,
            aoVerDetalhes = {
                sorteado = null // fecha o diálogo antes de navegar
                aoClicarEmItem(obra.id, tipo.valorTmdb)
            },
            aoSortearDeNovo = { sortear(excluir = obra) },
            aoFechar = { sorteado = null }
        )
    }
}

/**
 * Diálogo com o resultado do sorteio (RF65): capa, nome, ano e sinopse da obra,
 * com os botões "Ver detalhes" e "Sortear de novo".
 *
 * @param obra            obra sorteada.
 * @param aoVerDetalhes   abre a tela de Detalhes da obra.
 * @param aoSortearDeNovo sorteia outra obra, diferente desta.
 * @param aoFechar        fecha o diálogo (toque fora ou botão voltar).
 */
@Composable
private fun DialogoSorteio(
    obra: ResultadoBusca,
    aoVerDetalhes: () -> Unit,
    aoSortearDeNovo: () -> Unit,
    aoFechar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = aoFechar,
        title = { Text("O que assistir hoje?") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Capa em tamanho médio, com as bordas arredondadas do app (RNF10)
                AsyncImage(
                    model = obra.caminhoCapa?.let { "https://image.tmdb.org/t/p/w342$it" },
                    contentDescription = obra.nome,
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .aspectRatio(2f / 3f)
                        .capaDeObra(),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
                // Nome, com o ano entre parênteses quando a TMDB informa a data
                val ano = obra.data?.take(4)?.takeIf { it.isNotBlank() }
                Text(
                    text = if (ano != null) "${obra.nome} ($ano)" else obra.nome,
                    style = MaterialTheme.typography.titleMedium
                )
                // Sinopse resumida; só aparece se a TMDB tiver texto em português
                if (obra.sinopse.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = obra.sinopse,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = aoVerDetalhes) { Text("Ver detalhes") }
        },
        dismissButton = {
            TextButton(onClick = aoSortearDeNovo) { Text("Sortear de novo") }
        }
    )
}

/**
 * Menu suspenso no estilo da aba Descobrir: caixa com borda arredondada, o texto da
 * opção escolhida e uma seta; ao tocar, abre a lista de opções.
 *
 * @param rotulo      texto da opção atualmente escolhida.
 * @param opcoes      textos das opções, na ordem em que aparecem.
 * @param aoEscolher  recebe a posição (0, 1, 2...) da opção tocada.
 */
@Composable
private fun MenuFiltro(
    rotulo: String,
    opcoes: List<String>,
    aoEscolher: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Controla se a lista de opções está aberta
    var aberto by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { aberto = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(start = 12.dp, end = 4.dp)
        ) {
            // O texto ocupa o espaço que sobra e some com "..." se for longo (ex.: "Amazon Prime Video")
            Text(
                text = rotulo,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        }

        DropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
            opcoes.forEachIndexed { posicao, texto ->
                DropdownMenuItem(
                    text = { Text(texto) },
                    onClick = {
                        aberto = false
                        aoEscolher(posicao)
                    }
                )
            }
        }
    }
}

/**
 * Capa de uma obra na grade da Descobrir: só a imagem (sem título), com as bordas
 * arredondadas do app (RNF10). A imagem é de 342 px, pois cada capa é pequena em 4 colunas
 * mas a tela tem alta densidade de pixels.
 */
@Composable
private fun CapaDaGrade(resultado: ResultadoBusca, aoClicar: () -> Unit) {
    AsyncImage(
        model = resultado.caminhoCapa?.let { "https://image.tmdb.org/t/p/w342$it" },
        // O nome vai como descrição para leitores de tela, já que não aparece escrito
        contentDescription = resultado.nome,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2f / 3f)
            .capaDeObra()
            .clickable { aoClicar() },
        contentScale = ContentScale.Crop
    )
}