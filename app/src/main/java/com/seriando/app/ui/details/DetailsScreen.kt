package com.seriando.app.ui.details

import android.content.Intent
import android.net.Uri
import android.util.Log
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
import androidx.compose.ui.draw.clip
import com.seriando.app.model.VotoResumo

@Composable
fun DetailsScreen(serieId: Int, tipoMidia: String) {

    val context = LocalContext.current
    val authRepository = remember { AuthRepository() }
    val minhaListaRepository = remember { MinhaListaRepository() }
    val progressoRepository = remember { ProgressoRepository() }
    val avaliacaoRepository = remember { AvaliacaoRepository() }
    val perfilRepository = remember { PerfilRepository() }
    val scope = rememberCoroutineScope()

    var detalheSerie by remember { mutableStateOf<SerieDetalhe?>(null) }
    var detalheFilme by remember { mutableStateOf<MovieDetalhe?>(null) }
    var erro by remember { mutableStateOf(false) }
    var mostrarDialogoLogin by remember { mutableStateOf(false) }

    var adicionada by remember { mutableStateOf(false) }
    var statusAtualLabel by remember { mutableStateOf("Quero assistir") }

    var temporadaSelecionada by remember { mutableStateOf<Int?>(null) }
    var episodiosDaTemporada by remember { mutableStateOf<List<EpisodioResumo>>(emptyList()) }
    var assistidos by remember { mutableStateOf<Set<String>>(emptySet()) }

    var minhaNota by remember { mutableStateOf(0) }
    var mediaComunidade by remember { mutableStateOf(0.0) }
    var totalAvaliacoes by remember { mutableStateOf(0) }

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


    // Busca os detalhes (série ou filme, conforme o tipo).
    LaunchedEffect(serieId, tipoMidia) {
        try {
            if (tipoMidia == "movie") {
                detalheFilme = RetrofitInstance.api.buscarDetalhesFilme(serieId, BuildConfig.TMDB_API_KEY)
            } else {
                detalheSerie = RetrofitInstance.api.buscarDetalhesSerie(serieId, BuildConfig.TMDB_API_KEY)
            }
        } catch (e: Exception) {
            Log.e("Seriando", "Erro ao buscar detalhes", e)
            erro = true
        }
    }

    // Verifica se já está na minha lista e, se estiver, carrega o status salvo.
    LaunchedEffect(serieId) {
        val uid = authRepository.usuarioAtual()?.uid
        if (uid != null) {
            val serieSalva = minhaListaRepository.buscarSeries(uid).find { it.id == serieId }
            if (serieSalva != null) {
                adicionada = true
                statusAtualLabel = labelDoStatus(tipoMidia, serieSalva.status)
            }
        }
    }

    // Episódios assistidos, só para séries.
    LaunchedEffect(detalheSerie) {
        val uid = authRepository.usuarioAtual()?.uid
        if (tipoMidia == "tv" && uid != null) {
            assistidos = progressoRepository.buscarAssistidos(uid, serieId)
        }
    }

    // Nota do usuário e média da comunidade.
    LaunchedEffect(serieId, tipoMidia) {
        val uid = authRepository.usuarioAtual()?.uid
        if (uid != null) {
            minhaNota = avaliacaoRepository.buscarMinhaAvaliacao(uid, tipoMidia, serieId)?.nota ?: 0
        }
        val todas = avaliacaoRepository.buscarTodasAvaliacoes(tipoMidia, serieId).filter { it.nota > 0 }
        totalAvaliacoes = todas.size
        mediaComunidade = if (todas.isEmpty()) 0.0 else todas.map { it.nota }.average()
    }

    // Opiniões da obra (RF21), a opinião do próprio usuário (se existir),
    // e o resumo de votos de cada opinião (RF26).
    LaunchedEffect(serieId, tipoMidia) {
        val uid = authRepository.usuarioAtual()?.uid
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
    }

    // Busca a disponibilidade de streaming para o Brasil (RF34, RN09).
    LaunchedEffect(serieId, tipoMidia) {
        try {
            val resposta = if (tipoMidia == "movie") {
                RetrofitInstance.api.buscarProvedoresFilme(serieId, BuildConfig.TMDB_API_KEY)
            } else {
                RetrofitInstance.api.buscarProvedoresSerie(serieId, BuildConfig.TMDB_API_KEY)
            }
            provedoresBrasil = resposta.results["BR"]
        } catch (e: Exception) {
            Log.e("Seriando", "Erro ao buscar provedores de streaming", e)
        }
    }

    fun adicionarAMinhaLista() {
        val uid = authRepository.usuarioAtual()?.uid ?: return
        val nome = detalheSerie?.nome ?: detalheFilme?.nome ?: return
        val sinopse = detalheSerie?.sinopse ?: detalheFilme?.sinopse ?: ""
        val capa = detalheSerie?.caminhoCapa ?: detalheFilme?.caminhoCapa
        scope.launch {
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
        }
    }

    fun removerDaMinhaLista() {
        val uid = authRepository.usuarioAtual()?.uid ?: return
        scope.launch {
            minhaListaRepository.removerSerie(uid, serieId)
            adicionada = false
        }
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
                            ElencoCard(ator)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (adicionada) {
                    OutlinedButton(
                        onClick = { removerDaMinhaLista() },
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
                                            val nomeStatus = nomeDoStatus(tipoMidia, label)
                                            minhaListaRepository.atualizarStatusTexto(uid, serieId, nomeStatus)
                                            statusAtualLabel = label
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
                                avaliacaoRepository.salvarNota(uid, tipoMidia, serieId, notaEscolhida)
                                minhaNota = notaEscolhida
                                val todas = avaliacaoRepository.buscarTodasAvaliacoes(tipoMidia, serieId).filter { it.nota > 0 }
                                totalAvaliacoes = todas.size
                                mediaComunidade = if (todas.isEmpty()) 0.0 else todas.map { it.nota }.average()
                            }
                        }
                    }
                )

                if (minhaNota > 0) {
                    TextButton(onClick = {
                        val uid = authRepository.usuarioAtual()?.uid ?: return@TextButton
                        scope.launch {
                            avaliacaoRepository.removerMinhaAvaliacao(uid, tipoMidia, serieId)
                            minhaNota = 0
                            val todas = avaliacaoRepository.buscarTodasAvaliacoes(tipoMidia, serieId).filter { it.nota > 0 }
                            totalAvaliacoes = todas.size
                            mediaComunidade = if (todas.isEmpty()) 0.0 else todas.map { it.nota }.average()
                        }
                    }) {
                        Text("Remover minha avaliação")
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (totalAvaliacoes > 0) {
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
                                    avaliacaoRepository.removerOpiniao(uid, tipoMidia, serieId)
                                    opinioes = avaliacaoRepository.buscarOpinioes(tipoMidia, serieId, uid)
                                    minhaOpiniaoExistente = null
                                    textoOpiniao = ""
                                    spoilerMarcado = false
                                    publicaMarcada = true
                                    emocoesSelecionadas = emptyList()
                                }
                            },
                            aoCurtir = {
                                val votanteUid = authRepository.usuarioAtual()?.uid
                                if (votanteUid == null) {
                                    mostrarDialogoLogin = true
                                } else {
                                    scope.launch {
                                        avaliacaoRepository.votar(opiniao.autorUid, tipoMidia, serieId, votanteUid, 1)
                                        val novoResumo = avaliacaoRepository.buscarResumoVotos(opiniao.autorUid, tipoMidia, serieId, votanteUid)
                                        votosPorOpiniao = votosPorOpiniao + (opiniao.autorUid to novoResumo)
                                    }
                                }
                            },
                            aoDescurtir = {
                                val votanteUid = authRepository.usuarioAtual()?.uid
                                if (votanteUid == null) {
                                    mostrarDialogoLogin = true
                                } else {
                                    scope.launch {
                                        avaliacaoRepository.votar(opiniao.autorUid, tipoMidia, serieId, votanteUid, -1)
                                        val novoResumo = avaliacaoRepository.buscarResumoVotos(opiniao.autorUid, tipoMidia, serieId, votanteUid)
                                        votosPorOpiniao = votosPorOpiniao + (opiniao.autorUid to novoResumo)
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

                if (serieAtual != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "Temporadas", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        serieAtual.temporadas.forEach { temporada ->
                            TextButton(onClick = {
                                temporadaSelecionada = temporada.numero
                                scope.launch {
                                    episodiosDaTemporada = RetrofitInstance.api.buscarTemporada(
                                        serieId,
                                        temporada.numero,
                                        BuildConfig.TMDB_API_KEY
                                    ).episodios
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
                                    progressoRepository.marcarTemporadaComoAssistida(
                                        uid, serieId, numeroTemporadaAtual, episodiosDaTemporada
                                    )
                                    assistidos = assistidos + episodiosDaTemporada.map {
                                        "${numeroTemporadaAtual}_${it.numero}"
                                    }
                                }
                            }
                        }) {
                            Text("Marcar temporada inteira como assistida")
                        }

                        episodiosDaTemporada.forEach { episodio ->
                            val chaveEpisodio = "${numeroTemporadaAtual}_${episodio.numero}"
                            val assistido = assistidos.contains(chaveEpisodio)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Thumbnail do episódio (RF47). Nem todo episódio tem imagem
                                // na TMDB, então o AsyncImage simplesmente fica em branco
                                // quando caminhoThumbnail é nulo, sem quebrar a tela.
                                AsyncImage(
                                    model = episodio.caminhoThumbnail?.let { "https://image.tmdb.org/t/p/w300$it" },
                                    contentDescription = episodio.nome,
                                    modifier = Modifier
                                        .width(100.dp)
                                        .aspectRatio(16f / 9f)
                                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Checkbox(
                                    checked = assistido,
                                    onCheckedChange = { marcado ->
                                        val uid = authRepository.usuarioAtual()?.uid
                                        if (uid == null) {
                                            mostrarDialogoLogin = true
                                        } else {
                                            scope.launch {
                                                if (marcado) {
                                                    progressoRepository.marcarAssistido(
                                                        uid,
                                                        serieId,
                                                        numeroTemporadaAtual,
                                                        episodio.numero
                                                    )
                                                    assistidos = assistidos + chaveEpisodio
                                                } else {
                                                    progressoRepository.desmarcarAssistido(
                                                        uid,
                                                        serieId,
                                                        numeroTemporadaAtual,
                                                        episodio.numero
                                                    )
                                                    assistidos = assistidos - chaveEpisodio
                                                }
                                            }
                                        }
                                    }
                                )

                                Text(
                                    text = "${episodio.numero}. ${episodio.nome}",
                                    modifier = Modifier.weight(1f) // deixa o texto ocupar o espaço restante, sem estourar a largura
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
            aoFechar = { mostrarDialogoLogin = false },
            aoLogarComSucesso = {
                mostrarDialogoLogin = false
                adicionarAMinhaLista()
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
                            android.widget.Toast.makeText(context, "Denúncia enviada.", android.widget.Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            android.util.Log.e("Seriando", "Erro ao denunciar", e)
                            android.widget.Toast.makeText(context, "Não foi possível enviar a denúncia.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        )
    }
}