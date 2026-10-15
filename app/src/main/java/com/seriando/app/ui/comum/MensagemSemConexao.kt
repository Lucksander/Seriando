package com.seriando.app.ui.comum

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Mensagem de falha com ícone e botão para tentar de novo (RF62). Usada nas telas que dependem
 * de internet (Home, busca e comunidade), no lugar de um texto solto.
 *
 * @param texto           o que aconteceu e o que o usuário ainda pode fazer.
 * @param aoTentarDeNovo  ação do botão "Tentar de novo"; null esconde o botão.
 * @param modifier        permite à tela ajustar o espaço ocupado.
 */
@Composable
fun MensagemSemConexao(
    texto: String,
    aoTentarDeNovo: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null, // decorativo: o texto abaixo já explica
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        if (aoTentarDeNovo != null) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = aoTentarDeNovo) {
                Text("Tentar de novo")
            }
        }
    }
}