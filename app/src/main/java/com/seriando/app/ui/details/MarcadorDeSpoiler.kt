package com.seriando.app.ui.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Marcadores de spoiler (RF20, RN07). Todos usam as cores de ERRO do tema (vermelho), já
// adaptadas ao claro e ao escuro, e o ícone de alerta, para lembrar um aviso de perigo.

/**
 * Selo pequeno "SPOILER", mostrado acima do texto quando a opinião com spoiler já foi revelada,
 * para o leitor lembrar que o que lê estraga a história.
 */
@Composable
fun SeloDeSpoiler(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = "SPOILER",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

/**
 * Aviso que ocupa o lugar do texto de uma opinião com spoiler ainda escondida (RN07).
 * Cartão vermelho com ícone de alerta; tocar em qualquer parte dele revela o texto.
 *
 * @param aoRevelar chamada quando o leitor toca para ler mesmo assim.
 */
@Composable
fun AvisoDeSpoiler(aoRevelar: () -> Unit, modifier: Modifier = Modifier) {
    val cores = MaterialTheme.colorScheme
    val forma = RoundedCornerShape(12.dp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(forma)
            .background(cores.errorContainer)
            .border(BorderStroke(1.dp, cores.error), forma)
            .clickable(onClickLabel = "Revelar o spoiler", role = Role.Button, onClick = aoRevelar)
            .padding(12.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Warning,
            contentDescription = null,
            tint = cores.error,
            modifier = Modifier.size(28.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "ALERTA DE SPOILER",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = cores.onErrorContainer
            )
            Text(
                text = "Esta opinião revela partes da história. Toque para ler mesmo assim.",
                style = MaterialTheme.typography.bodySmall,
                color = cores.onErrorContainer
            )
        }
        // Olho: indica que tocar mostra o conteúdo escondido
        Icon(
            imageVector = Icons.Outlined.Visibility,
            contentDescription = null,
            tint = cores.error
        )
    }
}

/**
 * Opção "Contém spoiler" do formulário de opinião. Desmarcada, é uma linha neutra; marcada,
 * vira um cartão vermelho de alerta, mostrando ao autor que o texto ficará escondido.
 *
 * @param marcado   se a opinião está marcada como spoiler.
 * @param aoMudar   recebe o novo valor ao tocar em qualquer parte da linha.
 */
@Composable
fun MarcadorDeSpoilerNoFormulario(
    marcado: Boolean,
    aoMudar: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val cores = MaterialTheme.colorScheme
    val forma = RoundedCornerShape(12.dp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(forma)
            .background(if (marcado) cores.errorContainer else cores.surfaceVariant)
            .border(
                BorderStroke(1.dp, if (marcado) cores.error else cores.outlineVariant),
                forma
            )
            // A linha toda é o alvo de toque; o Checkbox dentro só mostra o estado (onCheckedChange nulo)
            .toggleable(value = marcado, role = Role.Checkbox, onValueChange = aoMudar)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Checkbox(
            checked = marcado,
            onCheckedChange = null,
            modifier = Modifier.padding(8.dp),
            colors = CheckboxDefaults.colors(checkedColor = cores.error)
        )
        Icon(
            imageVector = Icons.Outlined.Warning,
            contentDescription = null,
            tint = if (marcado) cores.error else cores.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(
                text = "Contém spoiler",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (marcado) cores.onErrorContainer else cores.onSurface
            )
            Text(
                text = if (marcado) "O texto ficará escondido até o leitor tocar para revelar"
                else "Marque se sua opinião conta partes da história",
                style = MaterialTheme.typography.bodySmall,
                color = if (marcado) cores.onErrorContainer else cores.onSurfaceVariant
            )
        }
    }
}
