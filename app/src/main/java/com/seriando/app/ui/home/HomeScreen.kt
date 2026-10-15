package com.seriando.app.ui.home

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.seriando.app.R
import com.seriando.app.data.auth.AuthRepository
import com.seriando.app.data.cache.CatalogoCache
import com.seriando.app.data.repository.CatalogoRepository
import com.seriando.app.data.repository.PerfilRepository
import com.seriando.app.data.sync.MonitorConexao
import com.seriando.app.model.Categorias
import com.seriando.app.model.PerfilPublico
import com.seriando.app.model.ResultadoBusca
import com.seriando.app.ui.comum.MensagemSemConexao
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Tela inicial (RF11, RF44, RF45, RF46, RF62).
 * Uma LazyColumn com o cabeçalho (entrar/sair, perfil, pesquisar) seguido de
 * uma fileira horizontal por categoria. Se nenhuma categoria carregar (sem internet, por
 * exemplo), mostra uma mensagem com o botão "Tentar de novo"; quando a internet volta,
 * as categorias que falharam são buscadas de novo sozinhas.
 *
 * @param aoClicarEmPesquisar abre a tela de busca.
 * @param aoClicarEmItem      abre os Detalhes (id e tipo "movie"/"tv").
 * @param aoClicarEmVerMais   abre a lista completa de uma categoria (recebe o id dela).
 */
@Composable
fun HomeScreen(
    aoClicarEmPesquisar: () -> Unit,
    aoClicarEmItem: (Int, String) -> Unit,
    aoClicarEmVerMais: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository() }
    val perfilRepository = remember { PerfilRepository() }
    val catalogoRepository = remember { CatalogoRepository() }
    val webClientId = stringResource(R.string.default_web_client_id)

    var usuario by remember { mutableStateOf(authRepository.usuarioAtual()) }
    var perfilPublico by remember { mutableStateOf<PerfilPublico?>(null) }

    // true se há internet agora; quando muda, as categorias que falharam são tentadas de novo
    val online by MonitorConexao.online.collectAsState()

    // Sobe a cada toque em "Tentar de novo" e refaz a busca das categorias que falharam
    var tentativa by remember { mutableIntStateOf(0) }

    // Itens de cada categoria, por id. Chave ausente = carregando (null);
    // lista vazia = falha. Já nasce preenchido com o que estiver no CatalogoCache,
    // para a Home não piscar em "carregando" ao voltar de Detalhes ou de outra aba.
    val itensPorCategoria = remember {
        mutableStateMapOf<String, List<ResultadoBusca>>().apply {
            Categorias.todas.forEach { categoria ->
                CatalogoCache.obter(categoria.id)?.let { put(categoria.id, it) }
            }
        }
    }

    // Carrega as categorias que ainda não estão no mapa, todas em paralelo.
    // Roda ao abrir, a cada "Tentar de novo" e quando a conexão muda. As que falharam antes
    // voltam ao estado "carregando" e são buscadas de novo.
    // Cada uma tem seu try/catch: se uma falhar, as outras continuam normalmente.
    LaunchedEffect(tentativa, online) {
        Categorias.todas.forEach { categoria ->
            if (itensPorCategoria[categoria.id]?.isEmpty() == true) itensPorCategoria.remove(categoria.id)
        }

        var falhas = 0
        coroutineScope {
            Categorias.todas
                .filter { itensPorCategoria[it.id] == null }
                .forEach { categoria ->
                    launch {
                        try {
                            itensPorCategoria[categoria.id] =
                                catalogoRepository.buscarPrimeirosItens(categoria)
                        } catch (e: CancellationException) {
                            // A busca foi interrompida (a conexão mudou ou a tela saiu): não é falha
                            throw e
                        } catch (e: Exception) {
                            Log.e("HomeScreen", "Falha ao carregar categoria ${categoria.id}", e)
                            // Lista vazia faz a fileira (ou a mensagem geral) mostrar o erro
                            itensPorCategoria[categoria.id] = emptyList()
                            falhas++
                        }
                    }
                }
        }
        // Um único aviso, mesmo que várias categorias tenham falhado; sem internet a mensagem
        // na tela já explica, então o Toast fica só para falhas com internet
        if (falhas > 0 && online) {
            Toast.makeText(context, "Algumas categorias não carregaram.", Toast.LENGTH_SHORT).show()
        }
    }

    // Observa o perfil no banco local: o nome e a foto aparecem sozinhos quando o perfil chega
    LaunchedEffect(usuario?.uid) {
        val uid = usuario?.uid
        if (uid == null) {
            perfilPublico = null
        } else {
            perfilRepository.observarPerfilPublico(uid).collect { perfilPublico = it }
        }
    }

    // Nenhuma categoria carregou: em vez de várias fileiras de erro, uma mensagem só
    val todasFalharam = Categorias.todas.all { itensPorCategoria[it.id]?.isEmpty() == true }

    LazyColumn(modifier = Modifier.fillMaxSize()) {

        // Cabeçalho: entrar/sair, perfil e botão de pesquisar
        item {
            Column24 {
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SERIANDO",
                        style = MaterialTheme.typography.headlineLarge
                    )

                    if (usuario != null) {
                        TextButton(onClick = {
                            authRepository.sair()
                            usuario = null
                            perfilPublico = null
                        }) {
                            Text("Sair")
                        }
                    } else {
                        TextButton(onClick = {
                            scope.launch {
                                try {
                                    usuario = authRepository.entrarComGoogle(context, webClientId)
                                } catch (e: Exception) {
                                    Log.e("HomeScreen", "Falha no login", e)
                                    Toast.makeText(context, "Não foi possível entrar.", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        }) {
                            Text("Entrar")
                        }
                    }
                }

                // Nome e foto do usuário logado, só leitura (editar fica na aba Conta)
                val publico = perfilPublico
                if (usuario != null && publico != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (publico.avatarId == "google") {
                            AsyncImage(
                                model = usuario?.photoUrl,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(text = publico.avatarId, style = MaterialTheme.typography.headlineSmall)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Olá, ${publico.apelido}!",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Organize suas séries. Nunca perca onde parou.",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = aoClicarEmPesquisar,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Pesquisar série ou filme")
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (todasFalharam) {
            // Uma mensagem só, com ícone e o botão para tentar de novo (RF62)
            item {
                MensagemSemConexao(
                    texto = if (!online) {
                        "Sem conexão com a internet. Os destaques precisam de internet, " +
                                "mas sua lista continua disponível na aba Minha Lista."
                    } else {
                        "Não foi possível carregar os destaques agora."
                    },
                    aoTentarDeNovo = { tentativa++ }
                )
            }
        } else {
            // Uma fileira por categoria, na ordem definida em Categorias.todas.
            // A chave (id) mantém a posição de rolagem estável quando os dados chegam.
            items(Categorias.todas, key = { it.id }) { categoria ->
                CategoriaRow(
                    categoria = categoria,
                    itens = itensPorCategoria[categoria.id],
                    aoClicarEmItem = aoClicarEmItem,
                    aoClicarEmVerMais = { aoClicarEmVerMais(categoria.id) }
                )
            }
        }
    }
}

/**
 * Coluna com a margem lateral de 24dp usada no cabeçalho da Home.
 * Existe porque a LazyColumn não tem margem própria: as fileiras cuidam
 * da sua e o cabeçalho usa esta.
 */
@Composable
private fun Column24(conteudo: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        content = conteudo
    )
}