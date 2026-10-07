package com.seriando.app.ui.details

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.seriando.app.model.ProvedoresPais
import com.seriando.app.model.Provedor

// Seção "Onde assistir" (RF34): mostra os provedores de streaming
// disponíveis no Brasil, separados por assinatura/aluguel/compra,
// com atribuição à JustWatch (RNF09), exigida pelos termos da TMDB.
@Composable
fun SecaoOndeAssistir(provedoresBrasil: ProvedoresPais?) {
    val context = LocalContext.current

    // RN10: quando não há dados para o Brasil, mostra uma mensagem em
    // vez de simplesmente omitir a seção, para o usuário saber que
    // consultamos e não encontramos, não que esquecemos de mostrar.
    if (provedoresBrasil == null ||
        (provedoresBrasil.assinatura.isNullOrEmpty() && provedoresBrasil.aluguel.isNullOrEmpty() && provedoresBrasil.compra.isNullOrEmpty())
    ) {
        Text(
            text = "Sem informação de streaming no Brasil",
            style = MaterialTheme.typography.bodyMedium
        )
        return
    }

    LinhaDeProvedores("Assinatura", provedoresBrasil.assinatura)
    LinhaDeProvedores("Aluguel", provedoresBrasil.aluguel)
    LinhaDeProvedores("Compra", provedoresBrasil.compra)

    // Link para a página da TMDB, que por sua vez lista a JustWatch como fonte.
    provedoresBrasil.link?.let { link ->
        TextButton(onClick = {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
        }) {
            Text("Ver mais detalhes")
        }
    }

    // Atribuição obrigatória pelos termos de uso da TMDB para dados de streaming.
    Text(
        text = "Dados de streaming fornecidos por JustWatch.",
        style = MaterialTheme.typography.labelSmall
    )
}

@Composable
private fun LinhaDeProvedores(titulo: String, provedores: List<Provedor>?) {
    if (provedores.isNullOrEmpty()) return

    Text(text = titulo, style = MaterialTheme.typography.labelMedium)
    Spacer(modifier = Modifier.padding(top = 4.dp))
    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
        provedores.forEach { provedor ->
            AsyncImage(
                model = provedor.caminhoLogo?.let { "https://image.tmdb.org/t/p/w92$it" },
                contentDescription = provedor.nome,
                modifier = Modifier
                    .size(48.dp)
                    .padding(end = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }
    }
    Spacer(modifier = Modifier.padding(top = 8.dp))
}