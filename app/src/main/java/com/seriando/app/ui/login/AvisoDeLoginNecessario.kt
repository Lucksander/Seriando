package com.seriando.app.ui.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Aviso mostrado ao visitante nas telas que dependem de conta (RF54): uma mensagem
 * explicando o motivo e o botão "Entrar", que abre a folha inferior de login.
 *
 * A folha é aberta e fechada aqui dentro; a tela que usa este aviso só precisa reagir
 * ao login bem-sucedido.
 *
 * @param mensagem          texto explicando o que o visitante precisa da conta para fazer.
 * @param aoLogarComSucesso chamada depois do login dar certo; a tela usa para recompor e
 *                          trocar este aviso pelo conteúdo do usuário logado.
 */
@Composable
fun AvisoDeLoginNecessario(
    mensagem: String,
    aoLogarComSucesso: () -> Unit,
    modifier: Modifier = Modifier
) {
    // true enquanto a folha de login está aberta
    var mostrarFolha by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = mensagem, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = { mostrarFolha = true }) {
            Text("Entrar")
        }
    }

    if (mostrarFolha) {
        DialogoLoginObrigatorio(
            aoFechar = { mostrarFolha = false },
            aoLogarComSucesso = {
                mostrarFolha = false
                aoLogarComSucesso()
            }
        )
    }
}