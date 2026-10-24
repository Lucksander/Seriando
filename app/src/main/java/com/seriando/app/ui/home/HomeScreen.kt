package com.seriando.app.ui.home

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.sp
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
import com.seriando.app.ui.comum.LogoSeriando
import com.seriando.app.ui.comum.MensagemSemConexao
import com.seriando.app.ui.usuario.AvatarDoUsuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import com.seriando.app.data.cache.VersaoDaIdade

/**
 * Tela inicial (RF11, RF44, RF45, RF46, RF62).
 * Uma LazyColumn com o cabeçalho (logotipo, avatar ou entrar, pesquisar) seguido de
 * uma fileira horizontal por categoria. Se nenhuma categoria carregar (sem internet, por
 * exemplo), mostra uma mensagem com o botão "Tentar de novo"; quando a internet volta,
 * as categorias que falharam são buscadas de novo sozinhas.
 *
 * @param aoClicarEmPesquisar abre a tela de busca.
 * @param aoClicarEmItem      abre os Detalhes (id e tipo "movie"/"tv").
 * @param aoClicarEmVerMais   abre a lista completa de uma categoria (recebe o id dela).
 * @param aoClicarEmPerfil    abre a aba Conta; chamado ao tocar no avatar do cabeçalho.
 */
@Composable
fun HomeScreen(
    aoClicarEmPesquisar: () -> Unit,
    aoClicarEmItem: (Int, String) -> Unit,
    aoClicarEmVerMais: (String) -> Unit,
    aoClicarEmPerfil: () -> Unit = {}
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

    // Muda quando a idade do usuário muda (login, logout, data de nascimento): as categorias
    // guardadas foram filtradas com a idade anterior e precisam ser buscadas de novo (RF50)
    val versaoDaIdade by VersaoDaIdade.versao.collectAsState()
    var versaoVista by remember { mutableIntStateOf(versaoDaIdade) }

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
    LaunchedEffect(tentativa, online, versaoDaIdade) {
        // A idade mudou desde que esta tela começou: esvazia as categorias para carregar de novo
        if (versaoDaIdade != versaoVista) {
            itensPorCategoria.clear()
            versaoVista = versaoDaIdade
        }

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

        // Cabeçalho: logotipo, avatar (ou entrar) e botão de pesquisar
        item {
            Column24 {
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logotipo: o ícone do S faz o papel da primeira letra e "eriando" vem ao lado
                    LogoSeriando(alturaIcone = 48.dp, tamanhoTexto = 34.sp)

                    if (usuario != null) {
                        // Logado: avatar no canto direito; tocar nele abre a aba Conta.
                        // O "Sair" fica na aba Conta.
                        AvatarDoCabecalho(
                            fotoGoogle = usuario?.photoUrl,
                            perfil = perfilPublico,
                            aoClicar = aoClicarEmPerfil
                        )
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

                // Saudação do usuário logado, só leitura (editar fica na aba Conta)
                val publico = perfilPublico
                if (usuario != null && publico != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Olá, ${publico.apelido}!",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Organize suas séries. Nunca perca onde parou.",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Barra de busca: não é um campo de texto de verdade, só um atalho visual;
                // tocar em qualquer parte dela abre a tela de busca
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(onClickLabel = "Pesquisar série ou filme") { aoClicarEmPesquisar() }
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // contentDescription nulo: o texto ao lado já descreve a ação
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Pesquisar série ou filme...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
 * Avatar redondo do cabeçalho da Home: mostra a foto da conta Google ou a foto
 * escolhida no app, conforme o avatarId do perfil. Tocar nele abre a aba Conta.
 * Enquanto o perfil não chega do banco local, mostra um boneco genérico, para o
 * toque continuar funcionando.
 *
 * @param fotoGoogle endereço da foto da conta Google (usado quando avatarId == "google").
 * @param perfil     perfil público do usuário, ou null se ainda não carregou.
 * @param aoClicar   chamado ao tocar no avatar.
 */
@Composable
private fun AvatarDoCabecalho(
    fotoGoogle: Uri?,
    perfil: PerfilPublico?,
    aoClicar: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClickLabel = "Abrir minha conta") { aoClicar() },
        contentAlignment = Alignment.Center
    ) {
        if (perfil == null) {
            // Perfil ainda não carregado: boneco genérico
            Icon(Icons.Outlined.Person, contentDescription = "Minha conta")
        } else {
            // Foto do Google, foto do app ou, na falta delas, a inicial do apelido
            AvatarDoUsuario(
                avatarId = perfil.avatarId,
                apelido = perfil.apelido,
                fotoGoogle = fotoGoogle,
                modifier = Modifier.fillMaxSize(),
                descricao = "Minha conta"
            )
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