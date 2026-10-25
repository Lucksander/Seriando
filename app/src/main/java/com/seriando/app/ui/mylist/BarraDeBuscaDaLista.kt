package com.seriando.app.ui.mylist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.seriando.app.model.OrdemDaLista

/**
 * Barra de busca da Minha Lista (RF66), no mesmo visual da barra da Home e da Busca:
 * cantos de 16dp, fundo cinza do tema, lupa à esquerda e "X" para limpar quando há texto.
 * A lista filtra a cada letra digitada; a tecla de ação do teclado só fecha o teclado.
 *
 * @param texto   texto atual (estado mantido por quem chama).
 * @param aoMudar chamada a cada letra digitada ou ao limpar.
 */
@Composable
fun BarraDeBuscaDaLista(texto: String, aoMudar: (String) -> Unit) {
    val focoDoTeclado = LocalFocusManager.current

    BasicTextField(
        value = texto,
        onValueChange = aoMudar,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(start = 16.dp, end = 4.dp),
        singleLine = true,
        // O BasicTextField não vem com tema: define a cor do texto e do cursor
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        // Como a lista já filtra enquanto digita, "buscar" só fecha o teclado
        keyboardActions = KeyboardActions(onSearch = { focoDoTeclado.clearFocus() }),
        // Desenha o miolo da barra: lupa, texto (ou dica) e o X de limpar
        decorationBox = { campoDeTexto ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null, // a dica ao lado já descreve o campo
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // O campo ocupa o espaço que sobra; a dica só aparece com o campo vazio
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (texto.isEmpty()) {
                        Text(
                            text = "Buscar na minha lista...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    campoDeTexto()
                }
                // X de limpar: só com texto
                if (texto.isNotEmpty()) {
                    IconButton(onClick = { aoMudar("") }, modifier = Modifier.size(40.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Limpar busca",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    )
}

/**
 * Botão "Ordenar: ..." da Minha Lista (RF66). Tocar abre um menu com as ordens possíveis;
 * escolher uma fecha o menu e avisa por [aoEscolher].
 *
 * @param ordemAtual ordem em uso, mostrada no botão e marcada no menu.
 * @param aoEscolher recebe a ordem escolhida.
 */
@Composable
fun SeletorDeOrdemDaLista(ordemAtual: OrdemDaLista, aoEscolher: (OrdemDaLista) -> Unit) {
    // Controla se o menu está aberto
    var menuAberto by remember { mutableStateOf(false) }

    // O Box ancora o menu logo abaixo do botão
    Box {
        AssistChip(
            onClick = { menuAberto = true },
            label = { Text("Ordenar: ${ordemAtual.rotulo}") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Sort,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
        DropdownMenu(expanded = menuAberto, onDismissRequest = { menuAberto = false }) {
            OrdemDaLista.entries.forEach { ordem ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = ordem.rotulo,
                            // A ordem em uso fica na cor principal
                            color = if (ordem == ordemAtual) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        menuAberto = false
                        aoEscolher(ordem)
                    }
                )
            }
        }
    }
}
