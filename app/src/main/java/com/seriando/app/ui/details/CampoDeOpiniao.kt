package com.seriando.app.ui.details

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Campo onde o usuário escreve a opinião (RF19), no mesmo visual da barra de pesquisa da Home:
 * cantos arredondados de 16dp, fundo cinza do tema (surfaceVariant) e sem linha embaixo.
 * Ao receber o foco, ganha um contorno fino na cor principal para o usuário saber onde digita.
 *
 * O limite de caracteres (RN03) é aplicado aqui: o texto que passaria do limite é ignorado.
 *
 * @param texto    texto atual (estado mantido por quem chama).
 * @param aoMudar  chamada com o novo texto a cada letra digitada, já respeitando o [limite].
 * @param limite   máximo de caracteres; o contador no rodapé fica na cor de erro ao chegar nele.
 */
@Composable
fun CampoDeOpiniao(
    texto: String,
    aoMudar: (String) -> Unit,
    limite: Int = 500,
    modifier: Modifier = Modifier
) {
    // Só para desenhar o contorno quando o campo está em foco
    var emFoco by remember { mutableStateOf(false) }
    val forma = RoundedCornerShape(16.dp)
    val cores = MaterialTheme.colorScheme

    TextField(
        value = texto,
        onValueChange = { if (it.length <= limite) aoMudar(it) },
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { emFoco = it.isFocused }
            // Contorno: 1,5dp na cor principal com foco; invisível sem foco (largura 0)
            .border(
                width = if (emFoco) 1.5.dp else 0.dp,
                color = if (emFoco) cores.primary else Color.Transparent,
                shape = forma
            ),
        shape = forma,
        // A dica some quando o usuário começa a digitar
        placeholder = { Text("Escreva sua opinião...") },
        minLines = 4,
        maxLines = 8,
        // Contador no canto direito; vira vermelho ao atingir o limite
        supportingText = {
            Text(
                text = "${texto.length}/$limite",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                color = if (texto.length >= limite) cores.error else cores.onSurfaceVariant
            )
        },
        // Fundo cinza do tema e nenhuma linha de indicador (o formato arredondado já delimita)
        colors = TextFieldDefaults.colors(
            focusedContainerColor = cores.surfaceVariant,
            unfocusedContainerColor = cores.surfaceVariant,
            disabledContainerColor = cores.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            cursorColor = cores.primary,
            focusedPlaceholderColor = cores.onSurfaceVariant,
            unfocusedPlaceholderColor = cores.onSurfaceVariant
        )
    )
}
