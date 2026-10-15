package com.seriando.app.ui.details

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.seriando.app.BuildConfig
import com.seriando.app.data.auth.AuthRepository
import com.seriando.app.data.remote.RetrofitInstance
import com.seriando.app.data.repository.AvaliacaoRepository
import com.seriando.app.data.repository.MinhaListaRepository
import com.seriando.app.data.repository.PerfilRepository
import com.seriando.app.data.repository.ProgressoRepository
import com.seriando.app.model.Avaliacao
import com.seriando.app.model.EpisodioResumo
import com.seriando.app.model.MovieDetalhe
import com.seriando.app.model.SerieDetalhe
import com.seriando.app.model.SerieSalva
import com.seriando.app.model.StatusSerie
import com.seriando.app.model.labelDoStatus
import com.seriando.app.model.nomeDoStatus
import com.seriando.app.model.opcoesDeStatus
import com.seriando.app.ui.login.DialogoLoginObrigatorio
import kotlinx.coroutines.launch
import com.seriando.app.model.VotoResumo
import com.seriando.app.ui.home.DialogoRemoverDaLista
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.seriando.app.ui.theme.capaDeObra
import com.seriando.app.data.repository.ObraCacheRepository
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import com.seriando.app.data.sync.MonitorConexao
import com.seriando.app.ui.comum.MensagemSemConexao


/**
 * Tela de detalhes de uma mídia (série ou filme).
 *
 * Exibe informações completas da obra selecionada, como imagem de capa, títulos,
 * sinopse, pontuação média, trailer e elenco principal.
 *
 * Principais funcionalidades integradas nesta tela:
 * - Gerenciamento de lista pessoal: Adição e remoção da obra na "Minha Lista" e
 *   atualização do status de acompanhamento (ex: "Assistindo", "Quero assistir").
 * - Provedores de streaming: Exibição dos serviços de streaming disponíveis no Brasil.
 * - Avaliação do usuário e comunidade: Atribuição de nota por estrelas, cálculo da média da
 *   comunidade e registro de resenhas/opiniões detalhadas (com spoiler, reações emocionais e privacidade).
 * - Interação social: Votação (curtir/descurtir) em opiniões de outros usuários e denúncia de conteúdos inadequados.
 * - Controle de progresso (Séries): Seleção de temporadas e episódios com marcação de progresso individual ou por temporada completa.
 *
 * A comunidade (média, opiniões, votos) só é lida com o usuário logado (RN08):
 * as regras do Firestore recusam a leitura de quem não está autenticado.
 *
 * @param serieId Identificador único do item no TMDB (The Movie Database).
 * @param tipoMidia Tipo do conteúdo: `"tv"` para séries ou `"movie"` para filmes.
 * @param aoClicarEmAtor Callback disparado ao selecionar um integrante do elenco, repassando o ID do ator.
 */
@Composable
fun DetailsScreen(serieId: Int, tipoMidia: String, aoClicarEmAtor: (Int) -> Unit) {

    val context = LocalContext.current
    val authRepository = remember { AuthRepository() }
    val minhaListaRepository = remember { MinhaListaRepository() }
    val progressoRepository = remember { ProgressoRepository() }
    val avaliacaoRepository = remember { AvaliacaoRepository() }
    val perfilRepository = remember { PerfilRepository() }
    val obraCacheRepository = remember { ObraCacheRepository() }
    val scope = rememberCoroutineScope()

    // Lido a cada recomposição. Serve de chave dos efeitos da comunidade abaixo:
    // quando o usuário entra (pelo diálogo) ou sai, os efeitos rodam de novo.
    val logado = authRepository.usuarioAtual() != null

    var detalheSerie by remember { mutableStateOf<SerieDetalhe?>(null) }
    var detalheFilme by remember { mutableStateOf<MovieDetalhe?>(null) }
    var erro by remember { mutableStateOf(false) }
    var mostrarDialogoLogin by remember { mutableStateOf(false) }
    var mostrarDialogoRemover by remember { mutableStateOf(false) } // confirmação de "remover da lista"

    var adicionada by remember { mutableStateOf(false) }
    var statusAtualLabel by remember { mutableStateOf("Quero assistir") }

    var temporadaSelecionada by remember { mutableStateOf<Int?>(null) }
    var episodiosDaTemporada by remember { mutableStateOf<List<EpisodioResumo>>(emptyList()) }
    var assistidos by remember { mutableStateOf<Set<String>>(emptySet()) }

    var minhaNota by remember { mutableStateOf(0) }
    var mediaComunidade by remember { mutableStateOf(0.0) }
    var totalAvaliacoes by remember { mutableStateOf(0) }
    // true quando a leitura da comunidade falhou (rede ou permissão): mostra aviso em vez de "sem avaliações"
    var erroComunidade by remember { mutableStateOf(false) }
    // Sobe a cada toque em "Tentar de novo" e refaz a busca da comunidade
    var tentativaComunidade by remember { mutableIntStateOf(0) }
    // Estado da conexão: quando a internet volta ou cai, a comunidade é buscada de novo
    val online by MonitorConexao.online.collectAsState()

    var textoOpiniao by remember { mutableStateOf("") }
    var spoilerMarcado by remember { mutableStateOf(false) }
    var publicaMarcada by remember { mutableStateOf(true) }
    var emocoesSelecionadas by remember { mutableStateOf<List<String>>(emptyList()) }
    var opinioes by remember { mutableStateOf<List<Avaliacao>>(emptyList()) }
    var minhaOpiniaoExistente by remember { mutableStateOf<Avaliacao?>(null) }
    var editandoOpiniao by remember { mutableStateOf(false) }
    var votosPorOpiniao by remember { mutableStateOf<Map<String, VotoResumo>>(emptyMap()) }
    val denunciaRepository = remember { com.seriando.app.data.repository.DenunciaRepository() }
    var mostrarDialogoDenuncia by remember { mutableStateOf<String?>(null) } // guarda o uid do autor sendo denunciado
    var provedoresBrasil by remember { mutableStateOf<com.seriando.app.model.ProvedoresPais?>(null) }
    // true só quando o login foi pedido pelo botão "Adicionar à minha lista":
    // assim, depois de entrar, a obra só é adicionada nesse caso (e não ao avaliar, votar etc.)
    var adicionarAposLogin by remember { mutableStateOf(false) }

    /**
     * Registra a falha no Logcat e avisa o usuário com um Toast.
     * Usada em toda ação que pode falhar silenciosamente (rede, permissão).
     */
    fun avisarErro(mensagem: String, e: Exception) {
        Log.e("Seriando", mensagem, e)
        Toast.makeText(context, mensagem, Toast.LENGTH_SHORT).show()
    }

    /**
     * Recalcula a média e o total de avaliações da comunidade.
     * Pode lançar exceção: quem chama deve estar dentro de um try/catch.
     */
    suspend fun atualizarMedia() {
        val todas = avaliacaoRepository.buscarTodasAvaliacoes(tipoMidia, serieId).filter { it.nota > 0 }
        totalAvaliacoes = todas.size
        mediaComunidade = if (todas.isEmpty()) 0.0 else todas.map { it.nota }.average()
    }

    // Busca os detalhes (série ou filme, conforme o tipo).
    LaunchedEffect(serieId, tipoMidia) {
        try {
            // Rede primeiro; sem rede, usa a cópia guardada no aparelho (RF33)
            if (tipoMidia == "movie") {
                detalheFilme = obraCacheRepository.buscarDetalheFilme(serieId)
            } else {
                detalheSerie = obraCacheRepository.buscarDetalheSerie(serieId)
            }
        } catch (e: Exception) {
            Log.e("Seriando", "Erro ao buscar detalhes", e)
            erro = true
        }
    }

    // Verifica se já está na minha lista e, se estiver, carrega o status salvo.
    LaunchedEffect(serieId, logado) {
        val uid = authRepository.usuarioAtual()?.uid
        if (uid != null) {
            try {
                val serieSalva = minhaListaRepository.buscarSeries(uid).find { it.id == serieId }
                if (serieSalva != null) {
                    adicionada = true
                    statusAtualLabel = labelDoStatus(tipoMidia, serieSalva.status)
                }
            } catch (e: Exception) {
                Log.e("Seriando", "Erro ao buscar minha lista", e)
            }
        } else {
            // Sem login: nada na lista
            adicionada = false
            statusAtualLabel = "Quero assistir"
        }
    }

    // Episódios assistidos, só para séries.
    LaunchedEffect(detalheSerie, logado) {
        val uid = authRepository.usuarioAtual()?.uid
        if (tipoMidia == "tv" && uid != null) {
            try {
                assistidos = progressoRepository.buscarAssistidos(uid, serieId)
            } catch (e: Exception) {
                Log.e("Seriando", "Erro ao buscar episódios assistidos", e)
            }
        } else {
            assistidos = emptySet()
        }
    }

    // Nota do usuário e média da comunidade. A leitura da comunidade exige login (RN08).
    LaunchedEffect(serieId, tipoMidia, logado, tentativaComunidade, online) {
        val uid = authRepository.usuarioAtual()?.uid
        if (uid == null) {
            // Sem login: limpa tudo e NÃO consulta o Firestore
            minhaNota = 0
            mediaComunidade = 0.0
            totalAvaliacoes = 0
            erroComunidade = false
            return@LaunchedEffect
        }
        try {
            erroComunidade = false
            minhaNota = avaliacaoRepository.buscarMinhaAvaliacao(uid, tipoMidia, serieId)?.nota ?: 0
            atualizarMedia()
        } catch (e: Exception) {
            Log.e("Seriando", "Erro ao buscar avaliações da comunidade", e)
            erroComunidade = true
        }
    }

    // Opiniões da obra (RF21), a opinião do próprio usuário (se existir),
    // e o resumo de votos de cada opinião (RF26). Exige login (RN08).
    LaunchedEffect(serieId, tipoMidia, logado, tentativaComunidade, online) {
        val uid = authRepository.usuarioAtual()?.uid
        if (uid == null) {
            // Sem login: esvazia a comunidade e o formulário, sem consultar o Firestore
            opinioes = emptyList()
            minhaOpiniaoExistente = null
            votosPorOpiniao = emptyMap()
            textoOpiniao = ""
            spoilerMarcado = false
            publicaMarcada = true
            emocoesSelecionadas = emptyList()
            return@LaunchedEffect
        }
        try {
            opinioes = avaliacaoRepository.buscarOpinioes(tipoMidia, serieId, uid)
            minhaOpiniaoExistente = opinioes.find { it.autorUid == uid }
            minhaOpiniaoExistente?.let {
                textoOpiniao = it.texto
                spoilerMarcado = it.spoiler
                publicaMarcada = it.publica
                emocoesSelecionadas = it.emocoes
            }
            votosPorOpiniao = opinioes.associate {
                it.autorUid to avaliacaoRepository.buscarResumoVotos(it.autorUid, tipoMidia, serieId, uid)
            }
        } catch (e: Exception) {
            Log.e("Seriando", "Erro ao buscar opiniões", e)
            erroComunidade = true
        }
    }

    // Busca a disponibilidade de streaming para o Brasil (RF34, RN09).
    LaunchedEffect(serieId, tipoMidia) {
        try {
            val resposta = obraCacheRepository.buscarProvedores(tipoMidia, serieId)
            provedoresBrasil = resposta.results["BR"]
        } catch (e: Exception) {
            Log.e("Seriando", "Erro ao buscar provedores de streaming", e)
        }
    }

    /**
     * Adiciona a mídia atual à lista do usuário com o status inicial "Quero assistir".
     */
    fun adicionarAMinhaLista() {
        val uid = authRepository.usuarioAtual()?.uid ?: return
        val nome = detalheSerie?.nome ?: detalheFilme?.nome ?: return
        val sinopse = detalheSerie?.sinopse ?: detalheFilme?.sinopse ?: ""
        val capa = detalheSerie?.caminhoCapa ?: detalheFilme?.caminhoCapa
        scope.launch {
            try {
                minhaListaRepository.adicionarSerie(
                    uid,
                    SerieSalva(
                        id = serieId,
                        nome = nome,
                        sinopse = sinopse,
                        caminhoCapa = capa,
                        tipoMidia = tipoMidia,
                        status = StatusSerie.QUERO_ASSISTIR.name
                    )
                )
                adicionada = true
                statusAtualLabel = "Quero assistir"
            } catch (e: Exception) {
                avisarErro("Não foi possível adicionar à lista.", e)
            }
        }
    }

    /**
     * Remove a mídia atual da lista do usuário.
     */
    fun removerDaMinhaLista() {
        val uid = authRepository.usuarioAtual()?.uid ?: return
        scope.launch {
            try {
                minhaListaRepository.removerSerie(uid, serieId)
                adicionada = false
                // O progresso foi apagado junto: zera o contador e o status na tela
                assistidos = emptySet()
                statusAtualLabel = "Quero assistir"
            } catch (e: Exception) {
                avisarErro("Não foi possível remover da lista.", e)
            }
        }
    }

    /**
     * Garante que a série esteja na lista antes de gravar progresso nela (RF29).
     * Sem isso, marcar episódios de uma série fora da lista criaria progresso sem
     * documento pai, que o app não consegue achar nem apagar depois.
     * Entra com o status "Assistindo", já que o usuário está marcando episódios.
     * Pode lançar exceção: quem chama deve estar dentro de try/catch (como nos pontos abaixo).
     */
    suspend fun garantirSerieNaLista(uid: String) {
        if (adicionada) return

        // Se já está na lista mas a tela ainda não sabia, só atualiza o estado
        // (set() com a série inteira apagaria o status que o usuário já tinha)
        if (minhaListaRepository.contemSerie(uid, serieId)) {
            adicionada = true
            return
        }

        val nome = detalheSerie?.nome ?: throw IllegalStateException("Detalhes da série não carregados")
        minhaListaRepository.adicionarSerie(
            uid,
            SerieSalva(
                id = serieId,
                nome = nome,
                sinopse = detalheSerie?.sinopse ?: "",
                caminhoCapa = detalheSerie?.caminhoCapa,
                tipoMidia = tipoMidia,
                status = StatusSerie.ASSISTINDO.name
            )
        )
        adicionada = true
        statusAtualLabel = StatusSerie.ASSISTINDO.label
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        when {
            (detalheSerie == null && detalheFilme == null) && !erro -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            erro -> {
                Text("Não foi possível carregar os detalhes.")
            }

            else -> {
                val nome = detalheSerie?.nome ?: detalheFilme?.nome ?: ""
                val sinopse = detalheSerie?.sinopse ?: detalheFilme?.sinopse ?: ""
                val caminhoCapa = detalheSerie?.caminhoCapa ?: detalheFilme?.caminhoCapa
                val notaMedia = detalheSerie?.notaMedia ?: detalheFilme?.notaMedia ?: 0.0
                val elenco = detalheSerie?.credits?.cast ?: detalheFilme?.credits?.cast ?: emptyList()
                val trailer = (detalheSerie?.videos?.results ?: detalheFilme?.videos?.results ?: emptyList())
                    .find { it.site == "YouTube" && it.type == "Trailer" }
                val serieAtual = detalheSerie
                val filmeAtual = detalheFilme

                AsyncImage(
                    model = caminhoCapa?.let { "https://image.tmdb.org/t/p/w500$it" },
                    contentDescription = nome,
                    modifier = Modifier
                        .width(200.dp)
                        .aspectRatio(2f / 3f)
                        .capaDeObra()
                        .align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = nome, style = MaterialTheme.typography.headlineMedium)

                if (notaMedia > 0) {
                    Text(
                        text = "⭐ ${"%.1f".format(notaMedia)} / 10",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (serieAtual != null) {
                    Text(
                        text = "${serieAtual.numeroTemporadas} temporada(s) · ${serieAtual.numeroEpisodios} episódio(s)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${assistidos.size} de ${serieAtual.numeroEpisodios} episódios assistidos",
                        style = MaterialTheme.typography.bodySmall
                    )
                } else if (filmeAtual != null) {
                    Text(
                        text = filmeAtual.duracaoMinutos?.let { "$it min" } ?: "Duração não informada",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = sinopse.ifBlank { "Sem sinopse disponível." },
                    style = MaterialTheme.typography.bodyLarge
                )

                if (trailer != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://www.youtube.com/watch?v=${trailer.key}")
                            )
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Assistir trailer")
                    }
                }

                if (elenco.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(text = "Elenco", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        elenco.take(15).forEach { ator ->
                            ElencoCard(ator = ator, aoClicar = { aoClicarEmAtor(ator.id) })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (adicionada) {
                    OutlinedButton(
                        onClick = { mostrarDialogoRemover = true }, // pede confirmação antes
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Remover da minha lista")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Status", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        opcoesDeStatus(tipoMidia).forEach { label ->
                            FilterChip(
                                selected = statusAtualLabel == label,
                                onClick = {
                                    val uid = authRepository.usuarioAtual()?.uid
                                    if (uid != null) {
                                        scope.launch {
                                            try {
                                                val nomeStatus = nomeDoStatus(tipoMidia, label)
                                                minhaListaRepository.atualizarStatusTexto(uid, serieId, nomeStatus)
                                                statusAtualLabel = label
                                            } catch (e: Exception) {
                                                avisarErro("Não foi possível atualizar o status.", e)
                                            }
                                        }
                                    }
                                },
                                label = { Text(label) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            if (authRepository.usuarioAtual() != null) {
                                adicionarAMinhaLista()
                            } else {
                                adicionarAposLogin = true
                                mostrarDialogoLogin = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Adicionar à minha lista")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "Onde assistir", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                SecaoOndeAssistir(provedoresBrasil)

                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Sua avaliação", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                SeletorEstrelas(
                    notaAtual = minhaNota,
                    aoSelecionar = { notaEscolhida ->
                        val uid = authRepository.usuarioAtual()?.uid
                        if (uid == null) {
                            mostrarDialogoLogin = true
                        } else {
                            scope.launch {
                                try {
                                    avaliacaoRepository.salvarNota(uid, tipoMidia, serieId, notaEscolhida)
                                    minhaNota = notaEscolhida
                                    atualizarMedia()
                                } catch (e: Exception) {
                                    avisarErro("Não foi possível salvar a avaliação.", e)
                                }
                            }
                        }
                    }
                )

                if (minhaNota > 0) {
                    TextButton(onClick = {
                        val uid = authRepository.usuarioAtual()?.uid ?: return@TextButton
                        scope.launch {
                            try {
                                avaliacaoRepository.removerMinhaAvaliacao(uid, tipoMidia, serieId)
                                minhaNota = 0
                                atualizarMedia()
                            } catch (e: Exception) {
                                avisarErro("Não foi possível remover a avaliação.", e)
                            }
                        }
                    }) {
                        Text("Remover minha avaliação")
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Média da comunidade: três situações (sem login, falha na leitura, normal)
                if (!logado) {
                    Text(
                        text = "Entre na sua conta para ver as avaliações e opiniões da comunidade.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else if (erroComunidade) {
                    MensagemSemConexao(
                        texto = if (!online) {
                            "Sem conexão com a internet. A comunidade (avaliações e opiniões) precisa de internet."
                        } else {
                            "Não foi possível carregar a comunidade agora."
                        },
                        aoTentarDeNovo = { tentativaComunidade++ }
                    )
                } else if (totalAvaliacoes > 0) {
                    val palavraAvaliacao = if (totalAvaliacoes == 1) "avaliação" else "avaliações"
                    Text(
                        text = "Média da comunidade: ${"%.1f".format(mediaComunidade)} ⭐ ($totalAvaliacoes $palavraAvaliacao)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = "Ainda sem avaliações da comunidade.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Opiniões: só para usuário logado (RN08). Sem login, o aviso acima já explica.
                if (logado) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "Opiniões da comunidade", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (minhaOpiniaoExistente == null || editandoOpiniao) {
                        OutlinedTextField(
                            value = textoOpiniao,
                            onValueChange = { if (it.length <= 500) textoOpiniao = it },
                            label = { Text("Escreva sua opinião (máx. 500 caracteres)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(text = "${textoOpiniao.length}/500", style = MaterialTheme.typography.labelSmall)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = spoilerMarcado,
                                onCheckedChange = { spoilerMarcado = it }
                            )
                            Text("Contém spoiler")
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = publicaMarcada,
                                onCheckedChange = { publicaMarcada = it }
                            )
                            Text(if (publicaMarcada) "Pública" else "Privada")
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Como você se sentiu assistindo?", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(4.dp))

                        SeletorEmocoes(
                            selecionadas = emocoesSelecionadas,
                            aoMudar = { emocoesSelecionadas = it }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                val uid = authRepository.usuarioAtual()?.uid
                                if (uid == null) {
                                    mostrarDialogoLogin = true
                                } else if (textoOpiniao.isNotBlank()) {
                                    scope.launch {
                                        try {
                                            val perfil = perfilRepository.buscarPerfilPublico(uid)
                                            avaliacaoRepository.salvarOpiniao(
                                                uid, tipoMidia, serieId,
                                                textoOpiniao, spoilerMarcado, emocoesSelecionadas, publicaMarcada,
                                                apelidoAutor = perfil?.apelido ?: "Usuário",
                                                avatarAutor = perfil?.avatarId ?: "google"
                                            )
                                            opinioes = avaliacaoRepository.buscarOpinioes(tipoMidia, serieId, uid)
                                            minhaOpiniaoExistente = opinioes.find { it.autorUid == uid }
                                            editandoOpiniao = false
                                        } catch (e: Exception) {
                                            avisarErro("Não foi possível publicar a opinião.", e)
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Publicar opinião")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (opinioes.isEmpty()) {
                        Text(
                            text = "Nenhuma opinião ainda. Seja o primeiro a comentar!",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        opinioes.forEach { opiniao ->
                            OpiniaoCard(
                                opiniao = opiniao,
                                ehMinha = opiniao.autorUid == authRepository.usuarioAtual()?.uid,
                                resumoVotos = votosPorOpiniao[opiniao.autorUid] ?: VotoResumo(),
                                aoEditar = { editandoOpiniao = true },
                                aoRemover = {
                                    val uid = authRepository.usuarioAtual()?.uid ?: return@OpiniaoCard
                                    scope.launch {
                                        try {
                                            avaliacaoRepository.removerOpiniao(uid, tipoMidia, serieId)
                                            opinioes = avaliacaoRepository.buscarOpinioes(tipoMidia, serieId, uid)
                                            minhaOpiniaoExistente = null
                                            textoOpiniao = ""
                                            spoilerMarcado = false
                                            publicaMarcada = true
                                            emocoesSelecionadas = emptyList()
                                        } catch (e: Exception) {
                                            avisarErro("Não foi possível remover a opinião.", e)
                                        }
                                    }
                                },
                                aoCurtir = {
                                    val votanteUid = authRepository.usuarioAtual()?.uid
                                    if (votanteUid == null) {
                                        mostrarDialogoLogin = true
                                    } else {
                                        scope.launch {
                                            try {
                                                avaliacaoRepository.votar(opiniao.autorUid, tipoMidia, serieId, votanteUid, 1)
                                                val novoResumo = avaliacaoRepository.buscarResumoVotos(opiniao.autorUid, tipoMidia, serieId, votanteUid)
                                                votosPorOpiniao = votosPorOpiniao + (opiniao.autorUid to novoResumo)
                                            } catch (e: Exception) {
                                                avisarErro("Não foi possível registrar o voto.", e)
                                            }
                                        }
                                    }
                                },
                                aoDescurtir = {
                                    val votanteUid = authRepository.usuarioAtual()?.uid
                                    if (votanteUid == null) {
                                        mostrarDialogoLogin = true
                                    } else {
                                        scope.launch {
                                            try {
                                                avaliacaoRepository.votar(opiniao.autorUid, tipoMidia, serieId, votanteUid, -1)
                                                val novoResumo = avaliacaoRepository.buscarResumoVotos(opiniao.autorUid, tipoMidia, serieId, votanteUid)
                                                votosPorOpiniao = votosPorOpiniao + (opiniao.autorUid to novoResumo)
                                            } catch (e: Exception) {
                                                avisarErro("Não foi possível registrar o voto.", e)
                                            }
                                        }
                                    }
                                },
                                aoDenunciar = {
                                    if (authRepository.usuarioAtual() == null) {
                                        mostrarDialogoLogin = true
                                    } else {
                                        mostrarDialogoDenuncia = opiniao.autorUid
                                    }
                                }
                            )
                        }
                    }
                }

                if (serieAtual != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "Temporadas", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        serieAtual.temporadas.forEach { temporada ->
                            TextButton(onClick = {
                                temporadaSelecionada = temporada.numero
                                scope.launch {
                                    try {
                                        episodiosDaTemporada = obraCacheRepository.buscarEpisodios(serieId, temporada.numero)
                                    } catch (e: Exception) {
                                        avisarErro("Não foi possível carregar a temporada.", e)
                                    }
                                }
                            }) {
                                Text(temporada.nome)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val numeroTemporadaAtual = temporadaSelecionada
                    if (numeroTemporadaAtual != null && episodiosDaTemporada.isNotEmpty()) {
                        TextButton(onClick = {
                            val uid = authRepository.usuarioAtual()?.uid
                            if (uid == null) {
                                mostrarDialogoLogin = true
                            } else {
                                scope.launch {
                                    try {
                                        garantirSerieNaLista(uid) // série fora da lista entra como "Assistindo"
                                        progressoRepository.marcarTemporadaComoAssistida(
                                            uid, serieId, numeroTemporadaAtual, episodiosDaTemporada
                                        )
                                        assistidos = assistidos + episodiosDaTemporada.map {
                                            "${numeroTemporadaAtual}_${it.numero}"
                                        }
                                    } catch (e: Exception) {
                                        avisarErro("Não foi possível marcar a temporada.", e)
                                    }
                                }
                            }
                        }) {
                            Text("Marcar temporada inteira como assistida")
                        }

                        // Episódios (RF47, RF57): fileira horizontal de cartões grandes
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(episodiosDaTemporada, key = { it.numero }) { episodio ->
                                val chaveEpisodio = "${numeroTemporadaAtual}_${episodio.numero}"

                                CartaoEpisodio(
                                    episodio = episodio,
                                    assistido = assistidos.contains(chaveEpisodio),
                                    aoMarcar = { marcado ->
                                        val uid = authRepository.usuarioAtual()?.uid
                                        if (uid == null) {
                                            mostrarDialogoLogin = true
                                        } else {
                                            scope.launch {
                                                try {
                                                    if (marcado) {
                                                        // Série fora da lista entra como "Assistindo" (RF29)
                                                        garantirSerieNaLista(uid)
                                                        progressoRepository.marcarAssistido(
                                                            uid, serieId, numeroTemporadaAtual, episodio.numero
                                                        )
                                                        assistidos = assistidos + chaveEpisodio
                                                    } else {
                                                        progressoRepository.desmarcarAssistido(
                                                            uid, serieId, numeroTemporadaAtual, episodio.numero
                                                        )
                                                        assistidos = assistidos - chaveEpisodio
                                                    }
                                                } catch (e: Exception) {
                                                    avisarErro("Não foi possível atualizar o episódio.", e)
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoLogin) {
        DialogoLoginObrigatorio(
            aoFechar = {
                mostrarDialogoLogin = false
                adicionarAposLogin = false // cancelou: descarta a ação pendente
            },
            aoLogarComSucesso = {
                mostrarDialogoLogin = false
                // Só adiciona à lista se foi isso que o usuário pediu antes do login
                if (adicionarAposLogin) {
                    adicionarAposLogin = false
                    adicionarAMinhaLista()
                }
            }
        )
    }

    // Confirmação de remover da lista: avisa que o progresso será perdido
    if (mostrarDialogoRemover) {
        DialogoRemoverDaLista(
            tipoMidia = tipoMidia,
            aoFechar = { mostrarDialogoRemover = false },
            aoConfirmar = {
                mostrarDialogoRemover = false
                removerDaMinhaLista()
            }
        )
    }

    val autorDenunciado = mostrarDialogoDenuncia
    if (autorDenunciado != null) {
        DialogoDenuncia(
            aoFechar = { mostrarDialogoDenuncia = null },
            aoConfirmar = { motivo ->
                val denuncianteUid = authRepository.usuarioAtual()?.uid
                mostrarDialogoDenuncia = null
                if (denuncianteUid != null) {
                    scope.launch {
                        try {
                            denunciaRepository.denunciarOpiniao(
                                autorDenunciado, tipoMidia, serieId, denuncianteUid, motivo
                            )
                            Toast.makeText(context, "Denúncia enviada.", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            avisarErro("Não foi possível enviar a denúncia.", e)
                        }
                    }
                }
            }
        )
    }
}