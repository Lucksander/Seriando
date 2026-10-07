package com.seriando.app.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.seriando.app.model.Avaliacao
import com.seriando.app.model.EMOCOES_DISPONIVEIS

@Composable
fun OpiniaoCard(
    opiniao: Avaliacao,
    ehMinha: Boolean,
    aoEditar: () -> Unit,
    aoRemover: () -> Unit
) {
    var revelado by remember { mutableStateOf(!opiniao.spoiler) }

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (opiniao.avatarAutor != "google") {
                    Text(text = opiniao.avatarAutor, style = MaterialTheme.typography.titleMedium)
                } else {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
                Spacer(modifier = Modifier.padding(start = 6.dp))
                Text(text = opiniao.apelidoAutor, style = MaterialTheme.typography.titleSmall)

                // Mostra "Privada" só para o próprio autor, já que só ele
                // consegue ver essa opinião quando ela está privada.
                if (ehMinha && !opiniao.publica) {
                    Spacer(modifier = Modifier.padding(start = 6.dp))
                    Text(text = "(privada)", style = MaterialTheme.typography.labelSmall)
                }
            }

            if (opiniao.emocoes.isNotEmpty()) {
                Row(modifier = Modifier.padding(top = 6.dp)) {
                    opiniao.emocoes.forEach { idEmocao ->
                        val emocao = EMOCOES_DISPONIVEIS.find { it.id == idEmocao }
                        if (emocao != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(emocao.cor))
                                )
                                Text(text = " ${emocao.label}", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.padding(top = 6.dp))

            if (opiniao.spoiler && !revelado) {
                Text(
                    text = "Contém spoiler — toque para revelar",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable { revelado = true }
                )
            } else {
                Text(text = opiniao.texto, style = MaterialTheme.typography.bodyMedium)
            }

            if (ehMinha) {
                Row(modifier = Modifier.padding(top = 6.dp)) {
                    TextButton(onClick = aoEditar) { Text("Editar") }
                    TextButton(onClick = aoRemover) { Text("Remover") }
                }
            }
        }
    }
}