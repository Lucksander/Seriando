package com.seriando.app.ui.legal

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink

/**
 * Frase "Ao continuar, você concorda com a Política de Privacidade e com os Termos de Uso."
 * com os dois nomes tocáveis. Usada na introdução e na tela de entrada.
 *
 * A tela não conhece a navegação: quem abre os documentos é o SeriandoNavHost,
 * por meio dos dois parâmetros abaixo.
 *
 * @param aoAbrirTermos      chamada ao tocar em "Termos de Uso".
 * @param aoAbrirPrivacidade chamada ao tocar em "Política de Privacidade".
 */
@Composable
fun AvisoDeTermosEPolitica(
    aoAbrirTermos: () -> Unit,
    aoAbrirPrivacidade: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Estilo dos trechos tocáveis: cor da marca e sublinhado
    val estiloLink = TextLinkStyles(
        style = SpanStyle(
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline
        )
    )
    val texto = buildAnnotatedString {
        append("Ao continuar, você concorda com a ")
        // LinkAnnotation.Clickable chama o listener em vez de abrir o navegador
        withLink(
            LinkAnnotation.Clickable(
                tag = "privacidade",
                styles = estiloLink,
                linkInteractionListener = { aoAbrirPrivacidade() }
            )
        ) { append("Política de Privacidade") }
        append(" e com os ")
        withLink(
            LinkAnnotation.Clickable(
                tag = "termos",
                styles = estiloLink,
                linkInteractionListener = { aoAbrirTermos() }
            )
        ) { append("Termos de Uso") }
        append(".")
    }
    Text(
        text = texto,
        modifier = modifier,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}
