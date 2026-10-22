package com.seriando.app.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seriando.app.ui.comum.LogoSeriando

// TODO: trocar pelos endereços reais da Política de Privacidade e dos Termos de Uso
// (a política será obrigatória na publicação do Google Play, RNF12).
private const val URL_PRIVACIDADE = "https://example.com/privacidade"
private const val URL_TERMOS = "https://example.com/termos"

/**
 * Tela de entrada (RF53): logotipo, botão de login com Google e opção de entrar como visitante.
 *
 * @param aoEntrarComSucesso      chamada depois do login com Google dar certo.
 * @param aoContinuarComoVisitante chamada ao tocar em "Continuar como visitante" (RF54).
 *   A tela não conhece a navegação: quem decide o destino é o SeriandoNavHost.
 */
@Composable
fun EntradaScreen(
    aoEntrarComSucesso: () -> Unit,
    aoContinuarComoVisitante: () -> Unit
) {
    // Estado e ação do login, compartilhados com a folha inferior do RF54
    val login = rememberLoginComGoogle(aoLogarComSucesso = aoEntrarComSucesso)

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Conteúdo fora das barras do sistema (RNF14)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Topo: logotipo, com espaço flexível acima e abaixo para centralizar no conjunto
        Spacer(modifier = Modifier.weight(1f))
        LogoSeriando(alturaIcone = 64.dp, tamanhoTexto = 44.sp)
        Spacer(modifier = Modifier.weight(1f))

        // Ação principal: login com Google (desabilitado enquanto o login está em andamento)
        Button(
            onClick = login.entrar,
            enabled = !login.entrando,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(if (login.entrando) "Entrando..." else "Entrar com o Google")
        }

        // Ação secundária: segue para a Home sem conta (modo visitante)
        TextButton(
            onClick = aoContinuarComoVisitante,
            enabled = !login.entrando,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("Continuar como visitante")
        }

        // Rodapé: texto com links para a política de privacidade e os termos de uso
        Spacer(modifier = Modifier.height(16.dp))
        TextoLegal(modifier = Modifier.padding(bottom = 16.dp))
    }
}

// Texto do rodapé com dois trechos tocáveis. LinkAnnotation.Url abre o navegador sozinho.
@Composable
private fun TextoLegal(modifier: Modifier = Modifier) {
    // Estilo dos trechos tocáveis: cor da marca e sublinhado
    val estiloLink = TextLinkStyles(
        style = SpanStyle(
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline
        )
    )
    val texto = buildAnnotatedString {
        append("Ao continuar, você concorda com a ")
        withLink(LinkAnnotation.Url(URL_PRIVACIDADE, estiloLink)) { append("Política de Privacidade") }
        append(" e com os ")
        withLink(LinkAnnotation.Url(URL_TERMOS, estiloLink)) { append("Termos de Uso") }
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