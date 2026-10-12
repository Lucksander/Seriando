package com.seriando.app.ui.home

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * Confirmação antes de remover um item da lista pessoal (RF07, RF29).
 * Avisa que o progresso será perdido, porque a remoção apaga o status e,
 * nas séries, os episódios marcados como assistidos.
 * A avaliação e a opinião da obra não são afetadas: ficam em outro lugar.
 *
 * @param tipoMidia   "tv" ou "movie", para ajustar o texto do aviso.
 * @param aoConfirmar chamado ao tocar em "Remover".
 * @param aoFechar    chamado ao cancelar ou tocar fora do diálogo.
 */
@Composable
fun DialogoRemoverDaLista(
    tipoMidia: String,
    aoConfirmar: () -> Unit,
    aoFechar: () -> Unit
) {
    val aviso = if (tipoMidia == "tv") {
        "O status e os episódios que você marcou como assistidos serão perdidos."
    } else {
        "O status deste filme será perdido."
    }

    AlertDialog(
        onDismissRequest = aoFechar,
        title = { Text("Remover da sua lista?") },
        text = { Text("$aviso Sua avaliação e sua opinião não serão afetadas. Essa ação não pode ser desfeita.") },
        confirmButton = {
            TextButton(onClick = aoConfirmar) { Text("Remover") }
        },
        dismissButton = {
            TextButton(onClick = aoFechar) { Text("Cancelar") }
        }
    )
}