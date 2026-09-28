package com.seriando.app.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Tela inicial do app.
// Agora ela só mostra o título, a lista "Minhas séries" (ainda vazia)
// e um botão que leva para a tela de pesquisa.
// aoClicarEmPesquisar é passado pelo NavHost: a Home não sabe navegar
// sozinha, ela só avisa que o botão foi clicado.
@Composable
fun HomeScreen(aoClicarEmPesquisar: () -> Unit) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "SERIANDO",
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "Organize suas séries. Nunca perca onde parou.",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botão que dispara a navegação para a tela de busca.
        Button(
            onClick = aoClicarEmPesquisar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Pesquisar série")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Minhas séries",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Placeholder: quando a lista pessoal existir (Etapa 5),
        // isso vira uma lista real de séries salvas pelo usuário.
        Text(
            text = "Você ainda não adicionou nenhuma série.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}