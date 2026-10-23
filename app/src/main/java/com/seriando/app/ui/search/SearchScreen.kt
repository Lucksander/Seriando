package com.seriando.app.ui.search

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.seriando.app.BuildConfig
import com.seriando.app.data.cache.BuscaCache
import com.seriando.app.data.local.HistoricoBusca
import com.seriando.app.data.local.ObraVista
import com.seriando.app.data.remote.RetrofitInstance
import com.seriando.app.data.repository.FiltroDeIdadeRepository
import com.seriando.app.data.sync.MonitorConexao
import com.seriando.app.ui.comum.MensagemSemConexao
import com.seriando.app.ui.home.ResultadoCard
import com.seriando.app.ui.theme.capaDeObra
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableIntStateOf
import com.seriando.app.data.cache.VersaoDaIdade

/**
 * Tela de pesquisa de séries e filmes (RF03, RF04).
 * A pesquisa é disparada pela tecla de pesquisa do teclado. Com o campo vazio, mostra o
 * histórico guardado no aparelho: as obras vistas recentemente (capas) e as pesquisas
 * recentes (texto), cada um com seu "Limpar". Estados: pesquisando (indicador), falha
 * (mensagem + Toast), nenhum resultado e lista de resultados. O texto e os resultados
 * ficam no BuscaCache para sobreviver à ida e volta entre telas.
 * Os resultados passam pelo filtro por idade (RF50): obras acima da idade do usuário
 * não aparecem, nem as que vieram do cache de uma pesquisa anterior.
 *
 * @param aoClicarEmItem abre os Detalhes (id e tipo "movie"/"tv").
 */
@Composable
fun SearchScreen(aoClicarEmItem: (Int, String) -> Unit) {

    val context = LocalContext.current

    // Usado para tirar o foco do campo (e com isso fechar o teclado) depois de pesquisar
    val focusManager = LocalFocusManager.current

    // true se há internet agora (muda o texto da mensagem de falha)
    val online by MonitorConexao.online.collectAsState()

    // O valor inicial vem do cache, não de uma string vazia — assim,
    // se o usuário já pesquisou algo antes e voltou para cá, o campo
    // e os resultados aparecem como estavam.
    var textoPesquisa by remember { mutableStateOf(BuscaCache.textoPesquisa) }
    var resultados by remember { mutableStateOf(BuscaCache.resultados) }
    val scope = rememberCoroutineScope()

    // Filtro por idade (RF50): a busca esconde as obras acima da idade do usuário
    val filtroDeIdade = remember { FiltroDeIdadeRepository() }

    var pesquisando by remember { mutableStateOf(false) }   // chamada à TMDB em andamento
    var falhou by remember { mutableStateOf(false) }        // a última pesquisa deu erro
    // Distingue "ainda não pesquisei" de "pesquisei e não achei nada".
    // Começa true se já há resultados guardados no cache.
    var jaPesquisou by remember { mutableStateOf(BuscaCache.resultados.isNotEmpty()) }

    // Histórico guardado no aparelho. As listas em estado são relidas do histórico
    // a cada mudança, para a tela atualizar na hora.
    val historico = remember { HistoricoBusca(context) }
    var pesquisasRecentes by remember { mutableStateOf(historico.pesquisas()) }
    // Todas as obras guardadas no aparelho, e só as que a idade atual permite mostrar (RF50)
    var obrasGuardadas by remember { mutableStateOf(historico.obrasVistas()) }
    var obrasRecentes by remember { mutableStateOf<List<ObraVista>>(emptyList()) }

    // Ao abrir a tela, filtra de novo os resultados que vieram do cache: eles podem ser de uma
    // pesquisa feita antes de o usuário entrar, sair ou mudar a data de nascimento, e não podem
    // mostrar obras acima da idade de agora (RF50)
    // Muda quando a idade do usuário muda: o que foi pesquisado antes pode não valer mais (RF50)
    val versaoDaIdade by VersaoDaIdade.versao.collectAsState()
    var versaoVista by remember { mutableIntStateOf(versaoDaIdade) }

    LaunchedEffect(versaoDaIdade) {
        if (versaoDaIdade != versaoVista) {
            // O VersaoDaIdade já limpou o BuscaCache; aqui limpa o que a tela tem em memória
            textoPesquisa = ""
            resultados = emptyList()
            jaPesquisou = false
            falhou = false
            versaoVista = versaoDaIdade
        }
    }

    // Obras vistas recentemente: só aparecem as que a idade atual permite (RF50).
    // Roda de novo quando a lista guardada muda ou quando a idade muda.
    // Se a classificação não puder ser consultada, a obra fica oculta (na dúvida, esconde).
    LaunchedEffect(obrasGuardadas, versaoDaIdade) {
        try {
            obrasRecentes = filtroDeIdade.filtrar(
                obrasGuardadas,
                tipoDe = { it.tipoMidia },
                idDe = { it.id }
            )
        } catch (e: Exception) {
            Log.e("Seriando", "Erro ao filtrar as obras vistas", e)
            obrasRecentes = emptyList()
        }
    }

    /** Executa a pesquisa com o texto atual, tratando erro, carregamento e texto vazio. */
    fun pesquisar() {
        // Texto vazio: não gasta chamada à API, só avisa
        if (textoPesquisa.isBlank()) {
            Toast.makeText(context, "Digite o nome de uma série ou filme.", Toast.LENGTH_SHORT).show()
            return
        }
        if (pesquisando) return // evita toques repetidos

        val termo = textoPesquisa.trim()
        pesquisando = true
        falhou = false
        scope.launch {
            try {
                val resposta = RetrofitInstance.api.buscarMulti(
                    BuildConfig.TMDB_API_KEY,
                    termo
                )
                // Só filmes e séries, e só os que a idade do usuário permite (RF50)
                resultados = filtroDeIdade.filtrar(
                    resposta.results.filter { it.ehFilme || it.ehSerie },
                    tipoDe = { it.tipoMidia },
                    idDe = { it.id }
                )
                BuscaCache.resultados = resultados // salva o resultado também
                jaPesquisou = true
                // Só entra no histórico a pesquisa que deu certo e achou algo:
                // erros de digitação e falhas de rede não ficam guardados
                if (resultados.isNotEmpty()) {
                    historico.registrarPesquisa(termo)
                    pesquisasRecentes = historico.pesquisas()
                }
            } catch (e: Exception) {
                Log.e("Seriando", "Erro na pesquisa", e)
                falhou = true
                Toast.makeText(context, "Não foi possível pesquisar.", Toast.LENGTH_SHORT).show()
            } finally {
                pesquisando = false
            }
        }
    }

    /** Abre os Detalhes e registra a obra no topo das vistas recentemente. */
    fun abrirObra(obra: ObraVista) {
        historico.registrarObra(obra)
        obrasGuardadas = historico.obrasVistas()
        aoClicarEmItem(obra.id, obra.tipoMidia)
    }

    /** Toque numa pesquisa recente: preenche o campo e já pesquisa. */
    fun pesquisarTermo(termo: String) {
        textoPesquisa = termo
        BuscaCache.textoPesquisa = termo
        focusManager.clearFocus()
        pesquisar()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Antes do verticalScroll de propósito: a área rolável começa abaixo da barra de status
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Barra de pesquisa no mesmo visual da Home: cantos arredondados, fundo cinza do
        // tema e lupa à esquerda. É um campo de verdade, de linha única; a tecla de ação
        // do teclado vira "pesquisar" em vez de pular linha.
        BasicTextField(
            value = textoPesquisa,
            onValueChange = {
                textoPesquisa = it
                BuscaCache.textoPesquisa = it // mantém o cache atualizado a cada letra digitada
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(start = 16.dp, end = 4.dp),
            singleLine = true,
            // Cor do texto digitado e do cursor, já que o BasicTextField não vem com tema
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    pesquisar()
                    focusManager.clearFocus() // fecha o teclado para mostrar os resultados
                }
            ),
            // Desenha o miolo da barra: lupa, texto (ou dica) e o X de limpar
            decorationBox = { campoDeTexto ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // contentDescription nulo: a dica ao lado já descreve o campo
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // O campo ocupa o espaço que sobra; a dica só aparece com o campo vazio
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (textoPesquisa.isEmpty()) {
                            Text(
                                text = "Pesquisar série ou filme...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        campoDeTexto()
                    }
                    // X de limpar: só aparece quando há texto. Esvazia também os resultados,
                    // para a tela voltar ao histórico em vez de mostrar resultados antigos.
                    if (textoPesquisa.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                textoPesquisa = ""
                                BuscaCache.textoPesquisa = ""
                                resultados = emptyList()
                                BuscaCache.resultados = emptyList()
                                jaPesquisou = false
                                falhou = false
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Limpar pesquisa",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            // Campo vazio: mostra o histórico (ou nada, se ainda não há histórico)
            textoPesquisa.isBlank() -> {
                HistoricoRecentes(
                    obras = obrasRecentes,
                    pesquisas = pesquisasRecentes,
                    aoClicarEmObra = { abrirObra(it) },
                    aoLimparObras = {
                        historico.limparObras()
                        obrasGuardadas = emptyList()
                    },
                    aoClicarEmPesquisa = { pesquisarTermo(it) },
                    aoRemoverPesquisa = {
                        historico.removerPesquisa(it)
                        pesquisasRecentes = historico.pesquisas()
                    },
                    aoLimparPesquisas = {
                        historico.limparPesquisas()
                        pesquisasRecentes = emptyList()
                    }
                )
            }
            pesquisando -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            falhou -> {
                MensagemSemConexao(
                    texto = if (!online) {
                        "Sem conexão com a internet. A pesquisa precisa de internet."
                    } else {
                        "Não foi possível pesquisar agora."
                    },
                    aoTentarDeNovo = { pesquisar() }
                )
            }
            jaPesquisou && resultados.isEmpty() -> {
                Text(
                    text = "Nenhum resultado para essa pesquisa.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            else -> {
                resultados.forEach { resultado ->
                    ResultadoCard(
                        resultado = resultado,
                        aoClicar = {
                            val tipoMidia = if (resultado.ehFilme) "movie" else "tv"
                            abrirObra(
                                ObraVista(
                                    id = resultado.id,
                                    tipoMidia = tipoMidia,
                                    nome = resultado.nome,
                                    caminhoCapa = resultado.caminhoCapa
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * Histórico mostrado com o campo de pesquisa vazio: primeiro a fileira de capas das obras
 * vistas recentemente, depois a lista de pesquisas recentes. Uma seção vazia não aparece;
 * se as duas estiverem vazias, não desenha nada.
 *
 * @param obras              obras vistas, da mais recente para a mais antiga.
 * @param pesquisas          termos pesquisados, do mais recente para o mais antigo.
 * @param aoClicarEmObra     abre os Detalhes da obra tocada.
 * @param aoLimparObras      apaga todas as obras vistas.
 * @param aoClicarEmPesquisa preenche o campo com o termo e pesquisa.
 * @param aoRemoverPesquisa  apaga uma pesquisa só.
 * @param aoLimparPesquisas  apaga todas as pesquisas.
 */
@Composable
private fun HistoricoRecentes(
    obras: List<ObraVista>,
    pesquisas: List<String>,
    aoClicarEmObra: (ObraVista) -> Unit,
    aoLimparObras: () -> Unit,
    aoClicarEmPesquisa: (String) -> Unit,
    aoRemoverPesquisa: (String) -> Unit,
    aoLimparPesquisas: () -> Unit
) {
    // Seção 1: obras vistas recentemente, em fileira horizontal de capas
    if (obras.isNotEmpty()) {
        CabecalhoDaSecao(titulo = "Vistos recentemente", aoLimpar = aoLimparObras)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // Chave tipo + id: filme e série podem ter o mesmo id na TMDB
            items(obras, key = { "${it.tipoMidia}_${it.id}" }) { obra ->
                Column(
                    modifier = Modifier
                        .width(104.dp)
                        .clickable { aoClicarEmObra(obra) }
                ) {
                    AsyncImage(
                        model = obra.caminhoCapa?.let { "https://image.tmdb.org/t/p/w342$it" },
                        contentDescription = obra.nome,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f / 3f)
                            .capaDeObra(), // bordas arredondadas (RNF10)
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = obra.nome,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    // Seção 2: pesquisas recentes, uma por linha
    if (pesquisas.isNotEmpty()) {
        CabecalhoDaSecao(titulo = "Pesquisas recentes", aoLimpar = aoLimparPesquisas)
        pesquisas.forEach { termo ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { aoClicarEmPesquisa(termo) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // contentDescription nulo: o texto ao lado já descreve a linha
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = termo,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { aoRemoverPesquisa(termo) }) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Remover \"$termo\" do histórico",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Título de uma seção do histórico, com o botão "Limpar" à direita.
 *
 * @param titulo   texto da seção.
 * @param aoLimpar apaga todos os itens da seção.
 */
@Composable
private fun CabecalhoDaSecao(titulo: String, aoLimpar: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = titulo, style = MaterialTheme.typography.titleMedium)
        TextButton(onClick = aoLimpar) { Text("Limpar") }
    }
}