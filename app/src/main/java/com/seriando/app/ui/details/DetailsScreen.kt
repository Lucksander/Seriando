package com.seriando.app.ui.details

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.seriando.app.data.auth.AuthRepository
import com.seriando.app.data.repository.AvaliacaoRepository
import com.seriando.app.data.repository.MinhaListaRepository
import com.seriando.app.data.repository.ObraCacheRepository
import com.seriando.app.data.repository.PerfilRepository
import com.seriando.app.data.repository.ProgressoRepository
import com.seriando.app.data.sync.MonitorConexao
import com.seriando.app.model.Avaliacao
import com.seriando.app.model.ClassificacaoIndicativa
import com.seriando.app.model.EpisodioResumo
import com.seriando.app.model.MovieDetalhe
import com.seriando.app.model.SerieDetalhe
import com.seriando.app.model.SerieSalva
import com.seriando.app.model.StatusSerie
import com.seriando.app.model.VotoResumo
import com.seriando.app.model.labelDoStatus
import com.seriando.app.model.nomeDoStatus
import com.seriando.app.model.opcoesDeStatus
import com.seriando.app.ui.comum.MensagemSemConexao
import com.seriando.app.ui.home.DialogoRemoverDaLista
import com.seriando.app.ui.login.DialogoLoginObrigatorio
import com.seriando.app.ui.theme.capaDeObra
import kotlinx.coroutines.launch
import com.seriando.app.data.cache.VersaoDaIdade
import com.seriando.app.data.repository.FiltroDeIdadeRepository
import kotlinx.coroutines.CancellationException
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import com.seriando.app.data.local.PreferenciasApp
import com.seriando.app.ui.tour.LocalTour
import com.seriando.app.ui.tour.PassoDoTour
import com.seriando.app.ui.tour.TourOverlay
import com.seriando.app.ui.tour.alvoDoTour
import com.seriando.app.ui.tour.rememberTourState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

/**
 * Tela de detalhes de uma mídia (série ou filme).
 *
 * Exibe informações completas da obra selecionada, como imagem de capa, títulos,
 * ano, situação, classificação indicativa, sinopse, pontuação média, trailer e elenco principal.
 *
 * Principais funcionalidades integradas nesta tela:
 * - Gerenciamento de lista pessoal: Adição e remoção da obra na "Minha Lista" e
 *   atualização do status de acompanhamento (ex: "Assistindo", "Quero assistir").
 * - Provedores de streaming: Exibição dos serviços de streaming disponíveis no Brasil.
 * - Avaliação do usuário e comunidade: Atribuição de nota por estrelas, cálculo da média da
 *   comunidade e registro de resenhas/opiniões detalhadas (com spoiler, reações emocionais e privacidade).
 * - Interação social: Votação (curtir/descurtir) em opiniões de outros usuários e denúncia de conteúdos inadequados.
 * - Controle de progresso (Séries): Seleção de temporadas e episódios com marcação de progresso individual ou por temporada completa.
 * - Compartilhamento (RF73): botão ao lado do nome que abre o menu de compartilhar do Android
 *   com o nome da obra e o link da página dela na TMDB (funciona também para o visitante).
 * - Classificação indicativa do Brasil (RF49): selo ao lado do ano e da situação da obra.
 *
 * A comunidade (média, opiniões, votos) só é lida com o usuário logado (RN08):
 * as regras do Firestore recusam a leitura de quem não está autenticado.
 *
 * @param serieId Identificador único do item no TMDB (The Movie Database).
 * @param tipoMidia Tipo do conteúdo: `"tv"` para séries ou `"movie"` para filmes.
 * @param aoClicarEmAtor Callback disparado ao selecionar um integrante do elenco, repassando o ID do ator.
 */
@OptIn(ExperimentalFoundationApi::class) // BringIntoViewRequester, usado pelo tour
@Composable
fun DetailsScreen(
    serieId: Int,
    tipoMidia: String,
    aoClicarEmAtor: (Int) -> Unit,
    // Chamado pelo botão "Voltar" do aviso de obra bloqueada por idade (RF51)
    aoVoltar: () -> Unit = {}
) {

    val context = LocalContext.current
    val authRepository = remember { AuthRepository() }
    val minhaListaRepository = remember { MinhaListaRepository() }
    val progressoRepository = remember { ProgressoRepository() }
    val avaliacaoRepository = remember { AvaliacaoRepository() }
    val perfilRepository = remember { PerfilRepository() }
    val obraCacheRepository = remember { ObraCacheRepository() }
    // Controle de idade (RF51): decide se este usuário pode abrir esta obra
    val filtroDeIdade = remember { FiltroDeIdadeRepository() }
    val scope = rememberCoroutineScope()

    // Lido a cada recomposição. Serve de chave dos efeitos da comunidade abaixo:
    // quando o usuário entra (pelo diálogo) ou sai, os efeitos rodam de novo.
    val logado = authRepository.usuarioAtual() != null

    var detalheSerie by remember { mutableStateOf<SerieDetalhe?>(null) }
    var detalheFilme by remember { mutableStateOf<MovieDetalhe?>(null) }
    var erro by remember { mutableStateOf(false) }
    // Resultado do controle de idade (RF51): null = ainda conferindo, true = liberada, false = bloqueada
    var liberada by remember { mutableStateOf<Boolean?>(null) }
    // Muda quando a idade do usuário muda com a tela aberta: a conferência é refeita
    val versaoDaIdade by VersaoDaIdade.versao.collectAsState()
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
    // Paginação das opiniões: data a partir da qual a próxima página começa, se ainda há mais
    // para carregar e se um "Carregar mais" está em andamento (desativa o botão)
    var proximaDataOpinioes by remember { mutableStateOf<Long?>(null) }
    var temMaisOpinioes by remember { mutableStateOf(false) }
    var carregandoMaisOpinioes by remember { mutableStateOf(false) }
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
     * Abre o menu de compartilhar do Android com o nome da obra e o link da página dela na TMDB
     * (themoviedb.org/tv/ID ou /movie/ID: o tipoMidia "tv" ou "movie" já é o trecho certo da URL).
     * Não depende de login nem grava nada.
     */
    fun compartilharObra(nome: String) {
        try {
            val link = "https://www.themoviedb.org/$tipoMidia/$serieId"
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, nome)
                putExtra(Intent.EXTRA_TEXT, "Dá uma olhada em \"$nome\":\n$link")
            }
            context.startActivity(Intent.createChooser(intent, "Compartilhar"))
        } catch (e: Exception) {
            avisarErro("Não foi possível compartilhar.", e)
        }
    }

    /**
     * Recalcula a média e o total de avaliações da comunidade.
     * Pode lançar exceção: quem chama deve estar dentro de um try/catch.
     */
    suspend fun atualizarMedia() {
        // O servidor calcula a média e o total (agregação), sem baixar as avaliações (RF18)
        val resumo = avaliacaoRepository.buscarMediaDaComunidade(tipoMidia, serieId)
        totalAvaliacoes = resumo.total
        mediaComunidade = resumo.media
    }

    /**
     * Recarrega a lista de opiniões do zero: a minha (se houver) no topo e a primeira página
     * das públicas dos outros, e guarda onde a próxima página começa.
     * Pode lançar exceção: quem chama deve estar dentro de um try/catch.
     */
    suspend fun recarregarOpinioes(uid: String) {
        val pagina = avaliacaoRepository.buscarPrimeiraPaginaDeOpinioes(tipoMidia, serieId, uid)
        opinioes = pagina.opinioes
        proximaDataOpinioes = pagina.proximaData
        temMaisOpinioes = pagina.temMais
    }

    /**
     * Busca o resumo de votos (RF26) só das opiniões exibidas que ainda não o têm,
     * em vez de ler os votos de todas as opiniões da obra.
     */
    suspend fun carregarVotosFaltantes(uid: String) {
        opinioes.filter { it.autorUid !in votosPorOpiniao }.forEach { opiniao ->
            val resumo = avaliacaoRepository.buscarResumoVotos(opiniao.autorUid, tipoMidia, serieId, uid)
            votosPorOpiniao = votosPorOpiniao + (opiniao.autorUid to resumo)
        }
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

    // Controle de idade (RF51): quando os detalhes chegam, confere se a idade do usuário permite a obra.
    // Vale para qualquer caminho até aqui (Home, busca, lista, sorteio, filmografia, histórico).
    // Usa a classificação que veio nos detalhes; se a cópia é antiga e não a traz, consulta a TMDB.
    // Na dúvida (erro ou obra sem classificação), bloqueia para menores.
    LaunchedEffect(detalheSerie, detalheFilme, versaoDaIdade) {
        val serie = detalheSerie
        val filme = detalheFilme
        if (serie == null && filme == null) return@LaunchedEffect

        try {
            // Os detalhes trazem o campo da classificação? (cópias antigas do Room não trazem)
            val trazClassificacao = if (serie != null) {
                serie.classificacoes != null
            } else {
                filme?.datasDeLancamento != null
            }
            val classificacao = if (serie != null) {
                ClassificacaoIndicativa.daSerie(serie.classificacoes)
            } else {
                ClassificacaoIndicativa.doFilme(filme?.datasDeLancamento)
            }
            liberada = filtroDeIdade.obraPermitida(tipoMidia, serieId, classificacao, trazClassificacao)
        } catch (e: CancellationException) {
            throw e // a tela saiu: não é falha
        } catch (e: Exception) {
            Log.e("Seriando", "Erro no controle de idade", e)
            liberada = false // na dúvida, bloqueia
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
            recarregarOpinioes(uid)
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

    // Disponibilidade de streaming para o Brasil (RF34, RN09).
    // Agora ela vem junto dos detalhes (append_to_response), então este efeito espera os
    // detalhes chegarem. Só se eles não trouxerem o campo (cópia antiga do aparelho, de antes
    // desta mudança) é que sobra a chamada separada, que também serve de reserva offline.
    LaunchedEffect(serieId, tipoMidia, detalheSerie, detalheFilme) {
        // Detalhes ainda não chegaram: o efeito roda de novo quando chegarem
        if (detalheSerie == null && detalheFilme == null) return@LaunchedEffect
        try {
            val juntoComDetalhes = detalheSerie?.provedores ?: detalheFilme?.provedores
            val resposta = juntoComDetalhes ?: obraCacheRepository.buscarProvedores(tipoMidia, serieId)
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

    // ---------- Tour guiado dos Detalhes ----------
    val preferencias = remember { PreferenciasApp(context) }

    // Já viu esta parte do tour? null = lendo o DataStore. Lido uma vez ao abrir a tela.
    var tourDetalhesVisto by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(Unit) { tourDetalhesVisto = preferencias.tourDetalhesVisto.first() }

    // true depois que o tour começou nesta tela; sobrevive à rotação e impede reinício
    var tourJaIniciou by rememberSaveable { mutableStateOf(false) }

    // Um requester por alvo: antes de destacar uma seção, o tour a leva até a área visível
    val requesterOndeAssistir = remember { BringIntoViewRequester() }
    val requesterAvaliacao = remember { BringIntoViewRequester() }
    val requesterOpiniao = remember { BringIntoViewRequester() }

    // Os 3 passos. Os ids precisam ser iguais aos de Modifier.alvoDoTour mais abaixo.
    // O texto do último depende de o usuário estar logado (a comunidade exige login, RN08).
    val passosDoTour = remember {
        listOf(
            PassoDoTour(
                alvoId = "onde_assistir",
                titulo = "Onde assistir",
                texto = "Veja em quais serviços a obra está disponível no Brasil, para assinatura, aluguel ou compra.",
                aoMostrar = { requesterOndeAssistir.bringIntoView() }
            ),
            PassoDoTour(
                alvoId = "avaliacao",
                titulo = "Sua avaliação",
                texto = "Toque nas estrelas para dar de 1 a 5. Você pode mudar ou remover sua nota quando quiser.",
                aoMostrar = { requesterAvaliacao.bringIntoView() }
            ),
            if (logado) {
                PassoDoTour(
                    alvoId = "opiniao",
                    titulo = "Opiniões da comunidade",
                    texto = "Escreva a sua opinião, marque até 6 emoções, avise se tem spoiler e escolha se ela fica pública ou privada. Nas opiniões dos outros, você pode curtir, descurtir e denunciar.",
                    aoMostrar = { requesterOpiniao.bringIntoView() }
                )
            } else {
                PassoDoTour(
                    alvoId = "opiniao",
                    titulo = "Comunidade",
                    texto = "Entre na sua conta para ver as opiniões da comunidade e escrever a sua.",
                    aoMostrar = { requesterOpiniao.bringIntoView() }
                )
            }
        )
    }
    val tour = rememberTourState(passosDoTour)

    // Inicia o tour na primeira vez que uma obra é aberta: só com a obra liberada e o conteúdo
    // na tela (o alvo "avaliacao" existe para logado e visitante). A espera deixa a tela assentar.
    LaunchedEffect(tourDetalhesVisto, liberada) {
        if (tourDetalhesVisto == false && liberada == true && !tourJaIniciou) {
            snapshotFlow { tour.alvos.containsKey("avaliacao") }
                .filter { it }
                .first()
            delay(800)
            tourJaIniciou = true
            tour.iniciar()
        }
    }

    // O Provider entrega o estado do tour aos elementos marcados; o Box permite desenhar
    // o overlay por cima do conteúdo rolável
    CompositionLocalProvider(LocalTour provides tour) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // Antes do verticalScroll de propósito: a área rolável começa abaixo da barra
                    // de status, e o conteúdo não passa por baixo dos ícones ao rolar
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp)
            ) {
                when {
                    (detalheSerie == null && detalheFilme == null) && !erro -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }

                    erro -> {
                        Text("Não foi possível carregar os detalhes.")
                    }

                    // Controle de idade (RF51): os detalhes chegaram, mas a idade ainda está sendo conferida.
                    // Nada da obra é mostrado enquanto isso.
                    liberada == null -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }

                    // Obra acima da idade do usuário: o aviso substitui todo o conteúdo (RF51)
                    liberada == false -> {
                        ObraBloqueada(
                            classificacao = if (detalheSerie != null) {
                                ClassificacaoIndicativa.daSerie(detalheSerie?.classificacoes)
                            } else {
                                ClassificacaoIndicativa.doFilme(detalheFilme?.datasDeLancamento)
                            },
                            aoVoltar = aoVoltar
                        )
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

                        // Ano (ou intervalo de anos, nas séries), selo de situação e classificação indicativa.
                        // Os três podem ser null (dado ausente na TMDB ou cópia antiga do Room).
                        // Cada um é declarado uma única vez, aqui.
                        val periodo = if (serieAtual != null) {
                            periodoDaSerie(serieAtual.dataEstreia, serieAtual.dataUltimoEpisodio, serieAtual.situacao)
                        } else {
                            anoDaData(filmeAtual?.dataLancamento)
                        }
                        val situacao = if (serieAtual != null) {
                            situacaoDaSerie(serieAtual.situacao)
                        } else {
                            situacaoDoFilme(filmeAtual?.situacao)
                        }
                        // Classificação indicativa do Brasil (RF49); null se a TMDB não informar
                        val classificacao = if (serieAtual != null) {
                            ClassificacaoIndicativa.daSerie(serieAtual.classificacoes)
                        } else {
                            ClassificacaoIndicativa.doFilme(filmeAtual?.datasDeLancamento)
                        }

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

                        // Nome da obra com o botão de compartilhar (RF73) alinhado à direita
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = nome,
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.weight(1f) // o nome ocupa o espaço restante; o botão fica no canto
                            )
                            IconButton(onClick = { compartilharObra(nome) }) {
                                Icon(Icons.Outlined.Share, contentDescription = "Compartilhar")
                            }
                        }

                        // Linha com o ano, o selo de situação e a classificação indicativa;
                        // some inteira se não houver nenhum dos três
                        if (periodo != null || situacao != null || classificacao != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (periodo != null) {
                                    Text(text = periodo, style = MaterialTheme.typography.bodyMedium)
                                }
                                if (situacao != null) {
                                    SeloDeSituacao(situacao)
                                }
                                if (classificacao != null) {
                                    SeloDeClassificacao(classificacao)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

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
                                // Ícone de play à esquerda do texto
                                Icon(
                                    Icons.Outlined.PlayArrow,
                                    contentDescription = null, // decorativo: o texto do botão já descreve a ação
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
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
                                // Lixeira: combina com "Remover" e a confirmação continua sendo pedida
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
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
                                // Sinal de mais à esquerda do texto
                                Icon(
                                    Icons.Outlined.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                                Text("Adicionar à minha lista")
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        // A seção inteira (título e provedores) é um alvo do tour; o requester leva
                        // a seção até a tela antes de ela ser destacada
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .bringIntoViewRequester(requesterOndeAssistir)
                                .alvoDoTour("onde_assistir")
                        ) {
                            Text(text = "Onde assistir", style = MaterialTheme.typography.headlineSmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            SecaoOndeAssistir(provedoresBrasil)
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        // Título e estrelas formam um alvo do tour
                        Column(
                            modifier = Modifier
                                .bringIntoViewRequester(requesterAvaliacao)
                                .alvoDoTour("avaliacao")
                        ) {
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
                        }

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
                            // Para o visitante, o aviso e o botão Entrar são o alvo do tour
                            Column(
                                modifier = Modifier
                                    .bringIntoViewRequester(requesterOpiniao)
                                    .alvoDoTour("opiniao")
                            ) {
                                Text(
                                    text = "Entre na sua conta para ver as avaliações e opiniões da comunidade.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                // Botão que abre a folha de login (RF54). Não há ação pendente a retomar,
                                // por isso adicionarAposLogin fica falso
                                TextButton(onClick = {
                                    adicionarAposLogin = false
                                    mostrarDialogoLogin = true
                                }) {
                                    Text("Entrar")
                                }
                            }
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
                            Text(
                                text = "Opiniões da comunidade",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .bringIntoViewRequester(requesterOpiniao)
                                    .alvoDoTour("opiniao") // alvo do tour (usuário logado)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (minhaOpiniaoExistente == null || editandoOpiniao) {
                                // Campo arredondado no estilo da Home; já traz o contador e o limite de 500 (RN03)
                                CampoDeOpiniao(
                                    texto = textoOpiniao,
                                    aoMudar = { textoOpiniao = it }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Marcador de spoiler: vira um cartão vermelho de alerta quando marcado (RF20)
                                MarcadorDeSpoilerNoFormulario(
                                    marcado = spoilerMarcado,
                                    aoMudar = { spoilerMarcado = it }
                                )

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
                                                    recarregarOpinioes(uid)
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
                                                    recarregarOpinioes(uid)
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

                                // Ainda há opiniões mais antigas: carrega a próxima página (só as que
                                // faltam; evita repetir uma opinião que já está na tela)
                                if (temMaisOpinioes) {
                                    TextButton(
                                        enabled = !carregandoMaisOpinioes,
                                        onClick = {
                                            val uid = authRepository.usuarioAtual()?.uid ?: return@TextButton
                                            scope.launch {
                                                carregandoMaisOpinioes = true
                                                try {
                                                    val pagina = avaliacaoRepository.buscarMaisOpinioes(
                                                        tipoMidia, serieId, uid, proximaDataOpinioes
                                                    )
                                                    val novas = pagina.opinioes.filter { nova ->
                                                        opinioes.none { it.autorUid == nova.autorUid }
                                                    }
                                                    opinioes = opinioes + novas
                                                    proximaDataOpinioes = pagina.proximaData
                                                    temMaisOpinioes = pagina.temMais
                                                    carregarVotosFaltantes(uid)
                                                } catch (e: Exception) {
                                                    avisarErro("Não foi possível carregar mais opiniões.", e)
                                                } finally {
                                                    carregandoMaisOpinioes = false
                                                }
                                            }
                                        }
                                    ) {
                                        Text(if (carregandoMaisOpinioes) "Carregando..." else "Carregar mais opiniões")
                                    }
                                }
                            }
                        }

                        if (serieAtual != null) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(text = "Temporadas", style = MaterialTheme.typography.headlineSmall)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Temporadas: fileira horizontal de cartões selecionáveis, cada um com
                            // quantidade de episódios, ano e progresso assistido
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(serieAtual.temporadas, key = { it.numero }) { temporada ->
                                    CartaoTemporada(
                                        nome = temporada.nome,
                                        totalEpisodios = temporada.totalEpisodios,
                                        ano = anoDaData(temporada.dataExibicao),
                                        // As chaves de "assistidos" são "temporada_episodio": conta as desta temporada
                                        assistidos = assistidos.count { it.startsWith("${temporada.numero}_") },
                                        selecionada = temporadaSelecionada == temporada.numero,
                                        aoClicar = {
                                            temporadaSelecionada = temporada.numero
                                            // Esvazia a lista antes de carregar, para não mostrar por um instante
                                            // os episódios da temporada anterior sob o número da nova
                                            episodiosDaTemporada = emptyList()
                                            scope.launch {
                                                try {
                                                    episodiosDaTemporada = obraCacheRepository.buscarEpisodios(serieId, temporada.numero)
                                                } catch (e: Exception) {
                                                    avisarErro("Não foi possível carregar a temporada.", e)
                                                }
                                            }
                                        }
                                    )
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

            // Camada do tour, por cima de tudo. Só grava a flag se algum passo foi exibido.
            TourOverlay(
                estado = tour,
                aoEncerrar = { viuAlgumPasso ->
                    if (viuAlgumPasso) {
                        tourDetalhesVisto = true
                        scope.launch { preferencias.marcarTourDetalhesVisto() }
                    }
                }
            )
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
                            // RF68: a opinião denunciada some da tela na hora, sem nova consulta
                            opinioes = opinioes.filter { it.autorUid != autorDenunciado }
                            votosPorOpiniao = votosPorOpiniao - autorDenunciado
                            Toast.makeText(context, "Denúncia enviada. A opinião foi ocultada para você.", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            avisarErro("Não foi possível enviar a denúncia.", e)
                        }
                    }
                }
            }
        )
    }
}