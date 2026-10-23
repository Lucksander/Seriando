package com.seriando.app.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.seriando.app.model.ClassificacaoIndicativa

/** Família de cor do selo de situação. */
enum class TomDoSelo { VERDE, CINZA, VERMELHO, DESTAQUE }

/** Situação da obra já traduzida: o texto exibido e a cor do selo. */
data class SituacaoDaObra(val texto: String, val tom: TomDoSelo)

/**
 * Extrai o ano de uma data da TMDB ("2019-03-15" -> "2019").
 * Pega só os 4 primeiros caracteres (sem java.time, pois o minSdk é 24).
 * Devolve null se a data for nula, curta ou não começar com 4 dígitos.
 */
fun anoDaData(data: String?): String? {
    if (data == null || data.length < 4) return null
    val ano = data.substring(0, 4)
    return if (ano.all { it.isDigit() }) ano else null
}

/**
 * Texto do período de uma série:
 * - em exibição: "2019 – atual"
 * - finalizada ou cancelada: "2019 – 2024" (ou só "2019" se o ano final for igual ou faltar)
 * - demais situações: só o ano de estreia.
 * Devolve null se a série não tiver data de estreia.
 */
fun periodoDaSerie(dataEstreia: String?, dataUltimoEpisodio: String?, situacao: String?): String? {
    val inicio = anoDaData(dataEstreia) ?: return null
    return when (situacao) {
        "Returning Series" -> "$inicio – atual"
        "Ended", "Canceled" -> {
            val fim = anoDaData(dataUltimoEpisodio)
            if (fim != null && fim != inicio) "$inicio – $fim" else inicio
        }
        else -> inicio
    }
}

/**
 * Traduz a situação de uma série da TMDB. Valor desconhecido ou ausente = sem selo (null).
 * A TMDB não tem "renovada": o mais próximo é "Returning Series", mostrado como "Em exibição".
 */
fun situacaoDaSerie(situacao: String?): SituacaoDaObra? = when (situacao) {
    "Returning Series" -> SituacaoDaObra("Em exibição", TomDoSelo.VERDE)
    "Ended" -> SituacaoDaObra("Finalizada", TomDoSelo.CINZA)
    "Canceled" -> SituacaoDaObra("Cancelada", TomDoSelo.VERMELHO)
    "In Production" -> SituacaoDaObra("Em produção", TomDoSelo.DESTAQUE)
    "Planned" -> SituacaoDaObra("Planejada", TomDoSelo.DESTAQUE)
    "Pilot" -> SituacaoDaObra("Piloto", TomDoSelo.DESTAQUE)
    else -> null
}

/**
 * Traduz a situação de um filme. "Released" (lançado) não ganha selo:
 * só aparece quando o filme ainda não foi lançado ou foi cancelado.
 */
fun situacaoDoFilme(situacao: String?): SituacaoDaObra? = when (situacao) {
    "Rumored" -> SituacaoDaObra("Rumor", TomDoSelo.CINZA)
    "Planned" -> SituacaoDaObra("Planejado", TomDoSelo.DESTAQUE)
    "In Production" -> SituacaoDaObra("Em produção", TomDoSelo.DESTAQUE)
    "Post Production" -> SituacaoDaObra("Pós-produção", TomDoSelo.DESTAQUE)
    "Canceled" -> SituacaoDaObra("Cancelado", TomDoSelo.VERMELHO)
    else -> null
}

/**
 * Cor do selo no tema atual. No escuro, as cores fixas são clareadas em 35%
 * (mesma regra dos chips de emoção); DESTAQUE usa a cor principal do tema.
 */
@Composable
private fun corDoTom(tom: TomDoSelo): Color {
    val base = when (tom) {
        TomDoSelo.VERDE -> Color(0xFF2E7D32)
        TomDoSelo.CINZA -> Color(0xFF616161)
        TomDoSelo.VERMELHO -> Color(0xFFC62828)
        TomDoSelo.DESTAQUE -> return MaterialTheme.colorScheme.primary
    }
    val temaEscuro = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return if (temaEscuro) lerp(base, Color.White, 0.35f) else base
}

/**
 * Selo (chip) com a situação da obra: borda e texto na cor da família, fundo translúcido.
 */
@Composable
fun SeloDeSituacao(situacao: SituacaoDaObra) {
    val cor = corDoTom(situacao.tom)
    val forma = RoundedCornerShape(50)
    Box(
        modifier = Modifier
            .background(cor.copy(alpha = 0.12f), forma)
            .border(1.dp, cor, forma)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = situacao.texto, style = MaterialTheme.typography.labelMedium, color = cor)
    }
}

/**
 * Selo quadrado da classificação indicativa (RF49), com as cores usadas no Brasil:
 * L verde, 10 azul, 12 amarelo, 14 laranja, 16 vermelho e 18 preto.
 * Tem borda fina para o selo preto não sumir sobre o fundo escuro, e uma descrição
 * para leitores de tela, já que o rótulo sozinho ("12") não diz do que se trata.
 */
@Composable
fun SeloDeClassificacao(classificacao: ClassificacaoIndicativa) {
    // Cor do fundo e do texto (preto sobre amarelo e laranja, por contraste)
    val (fundo, textoCor) = when (classificacao.idadeMinima) {
        0 -> Color(0xFF00A650) to Color.White
        10 -> Color(0xFF0072BC) to Color.White
        12 -> Color(0xFFFFD200) to Color.Black
        14 -> Color(0xFFF7941D) to Color.Black
        16 -> Color(0xFFED1C24) to Color.White
        else -> Color(0xFF000000) to Color.White
    }
    val descricao = if (classificacao.idadeMinima == 0) {
        "Classificação indicativa: livre"
    } else {
        "Classificação indicativa: ${classificacao.idadeMinima} anos"
    }
    val forma = RoundedCornerShape(4.dp)
    Box(
        modifier = Modifier
            .size(28.dp)
            .background(fundo, forma)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, forma)
            .semantics { contentDescription = descricao },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = classificacao.rotulo,
            color = textoCor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}