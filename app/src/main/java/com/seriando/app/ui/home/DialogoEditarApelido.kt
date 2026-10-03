package com.seriando.app.ui.home

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// Diálogo simples para o usuário escolher como quer ser chamado no app.
// valorInicial preenche o campo já com o apelido atual, para edição.
@Composable
fun DialogoEditarApelido(
    valorInicial: String,
    aoFechar: () -> Unit,
    aoConfirmar: (String) -> Unit
) {
    var texto by remember { mutableStateOf(valorInicial) }

    AlertDialog(
        onDismissRequest = aoFechar,
        title = { Text("Como você quer ser chamado?") },
        text = {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                label = { Text("Apelido") }
            )
        },
        confirmButton = {
            TextButton(onClick = {
                if (texto.isNotBlank()) {
                    aoConfirmar(texto.trim())
                }
            }) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = aoFechar) {
                Text("Cancelar")
            }
        }
    )
}