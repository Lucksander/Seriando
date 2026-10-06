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
import com.seriando.app.data.repository.MinhaListaRepository
import com.seriando.app.data.repository.ProgressoRepository
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
import com.seriando.app.data.repository.AvaliacaoRepository

@Composable
fun DetailsScreen(serieId: Int, tipoMidia: String) {

    val context = LocalContext.current
    val authRepository = remember { AuthRepository() }
    val minhaListaRepository = remember { MinhaListaRepository() }
    val progressoRepository = remember { ProgressoRepository() }
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
    val avaliacaoRepository = remember { AvaliacaoRepository() }
    var minhaNota by remember { mutableStateOf(0) }
    var mediaComunidade by remember { mutableStateOf(0.0) }
    var totalAvaliacoes by remember { mutableStateOf(0) }

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

    // Verifica se já está salvo e, se estiver, carrega o status salvo.
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

    LaunchedEffect(detalheSerie) {
        val uid = authRepository.usuarioAtual()?.uid
        if (tipoMidia == "tv" && uid != null) {
            assistidos = progressoRepository.buscarAssistidos(uid, serieId)
        }
    }
    // Busca a nota do usuário e a média da comunidade para esta obra.
    LaunchedEffect(serieId, tipoMidia) {
        val uid = authRepository.usuarioAtual()?.uid
        if (uid != null) {
            minhaNota = avaliacaoRepository.buscarMinhaAvaliacao(uid, tipoMidia, serieId)?.nota ?: 0
        }
        val todas = avaliacaoRepository.buscarTodasAvaliacoes(tipoMidia, serieId).filter { it.nota > 0 }
        totalAvaliacoes = todas.size
        mediaComunidade = if (todas.isEmpty()) 0.0 else todas.map { it.nota }.average()
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

                // Nota média da comunidade (TMDB usa escala de 0 a 10).
                if (notaMedia > 0) {
                    Text(
                        text = "⭐ ${"%.1f".format(notaMedia)} / 10",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                val serieAtual = detalheSerie
                val filmeAtual = detalheFilme
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

                // Botão de trailer, só aparece se a TMDB tiver um disponível.
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

                // Elenco principal, em fileira horizontal.
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

                    // Chips de status: a lista de opções muda conforme é série ou filme.
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
                                // Recalcula a média, já que a nota do usuário mudou.
                                val todas = avaliacaoRepository.buscarTodasAvaliacoes(tipoMidia, serieId).filter { it.nota > 0 }
                                totalAvaliacoes = todas.size
                                mediaComunidade = if (todas.isEmpty()) 0.0 else todas.map { it.nota }.average()
                            }
                        }
                    }
                )

                // Permite remover a própria avaliação (RF17), só aparece se já existir uma.
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

                // Média da comunidade (RF18), só aparece se houver ao menos uma avaliação.
                if (totalAvaliacoes > 0) {
                    // Pluraliza corretamente: "1 avaliação" ou "N avaliações".
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
                        // Botão para marcar a temporada toda de uma vez.
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
                                                        uid, serieId, numeroTemporadaAtual, episodio.numero
                                                    )
                                                    assistidos = assistidos + chaveEpisodio
                                                } else {
                                                    progressoRepository.desmarcarAssistido(
                                                        uid, serieId, numeroTemporadaAtual, episodio.numero
                                                    )
                                                    assistidos = assistidos - chaveEpisodio
                                                }
                                            }
                                        }
                                    }
                                )
                                Text("${episodio.numero}. ${episodio.nome}")
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
}