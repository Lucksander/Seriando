package com.seriando.app.ui.usuario

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background

// Lista fixa de emojis disponíveis como avatar dentro do app (RF15).
// "google" é tratado à parte, como opção de usar a foto da conta Google.
val AVATARES_DISPONIVEIS = listOf("🦊", "🐱", "🐶", "🐼", "🦁", "🐸", "🐧", "🦄")

// Fileira de avatares selecionáveis. aoSelecionar recebe o emoji escolhido.
@Composable
fun SeletorAvatar(avatarSelecionado: String, aoSelecionar: (String) -> Unit) {
    Row {
        AVATARES_DISPONIVEIS.forEach { avatar ->
            val selecionado = avatar == avatarSelecionado
            Text(
                text = avatar,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .padding(4.dp)
                    .clickable { aoSelecionar(avatar) }
                    // Destaca visualmente o avatar selecionado, mudando o fundo.
                    .background(
                        if (selecionado) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    )
                    .padding(8.dp)
            )
        }
    }
}