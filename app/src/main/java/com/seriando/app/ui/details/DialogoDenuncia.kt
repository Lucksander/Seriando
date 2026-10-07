package com.seriando.app.ui.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seriando.app.model.MOTIVOS_DENUNCIA
import androidx.compose.foundation.layout.padding

// Diálogo para escolher o motivo da denúncia (RF28) antes de confirmar.
@Composable
fun DialogoDenuncia(aoFechar: () -> Unit, aoConfirmar: (String) -> Unit) {
    var motivoSelecionado by remember { mutableStateOf(MOTIVOS_DENUNCIA.first()) }

    AlertDialog(
        onDismissRequest = aoFechar,
        title = { Text("Denunciar opinião") },
        text = {
            Column {
                MOTIVOS_DENUNCIA.forEach { motivo ->
                    androidx.compose.foundation.layout.Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.selectable(
                            selected = motivo == motivoSelecionado,
                            onClick = { motivoSelecionado = motivo }
                        )
                    ) {
                        RadioButton(
                            selected = motivo == motivoSelecionado,
                            onClick = { motivoSelecionado = motivo }
                        )
                        Text(motivo, modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { aoConfirmar(motivoSelecionado) }) {
                Text("Denunciar")
            }
        },
        dismissButton = {
            TextButton(onClick = aoFechar) {
                Text("Cancelar")
            }
        }
    )
}