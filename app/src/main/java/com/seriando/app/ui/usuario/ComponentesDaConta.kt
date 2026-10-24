package com.seriando.app.ui.usuario

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// Peças visuais da aba Conta. Ficam separadas da UsuarioScreen para a tela principal
// cuidar só do estado e das gravações.

/**
 * Cabeçalho da aba Conta: avatar grande sobre um brilho suave (o mesmo da introdução),
 * com um selo de lápis no canto, e abaixo o apelido e o e-mail.
 * Tocar no avatar ou no selo abre a escolha de avatar.
 *
 * @param fotoGoogle     foto da conta Google (usada quando avatarId == "google").
 * @param aoEditarAvatar chamada ao tocar no avatar ou no selo.
 */
@Composable
fun CabecalhoDaConta(
    avatarId: String,
    apelido: String,
    email: String,
    fotoGoogle: Any?,
    aoEditarAvatar: () -> Unit
) {
    val corPrincipal = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Círculo com degradê radial na cor da marca, dando o "brilho" atrás do avatar
        Box(
            modifier = Modifier
                .size(176.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(corPrincipal.copy(alpha = 0.30f), Color.Transparent)
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.size(112.dp)) {
                // Avatar com anel na cor da marca; o clip antes do clickable limita o ripple ao círculo
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .clickable(onClickLabel = "Trocar avatar", onClick = aoEditarAvatar)
                        .border(width = 3.dp, color = corPrincipal, shape = CircleShape)
                ) {
                    AvatarDoUsuario(
                        avatarId = avatarId,
                        apelido = apelido,
                        fotoGoogle = fotoGoogle,
                        modifier = Modifier.fillMaxSize(),
                        descricao = "Seu avatar"
                    )
                }
                // Selo de lápis no canto inferior direito: indica que o avatar pode ser trocado
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(corPrincipal)
                        .border(width = 2.dp, color = MaterialTheme.colorScheme.background, shape = CircleShape)
                        .clickable(onClickLabel = "Trocar avatar", onClick = aoEditarAvatar),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Trocar avatar",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        Text(
            text = apelido,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        if (email.isNotBlank()) {
            Text(
                text = email,
                modifier = Modifier.padding(top = 2.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Seção da aba Conta: um título pequeno em maiúsculas, na cor da marca, e um cartão
 * (cor Velvet Surface) com as linhas da seção dentro.
 *
 * @param titulo   nome da seção (ex: "Perfil").
 * @param conteudo linhas do cartão, normalmente [LinhaDaConta] separadas por [SeparadorDeLinhas].
 */
@Composable
fun CartaoDeSecao(
    titulo: String,
    modifier: Modifier = Modifier,
    conteudo: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = titulo.uppercase(),
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(content = conteudo)
        }
    }
}

/**
 * Uma linha dentro de [CartaoDeSecao]: ícone, título, valor opcional embaixo e, à direita,
 * um texto de ação (ex: "Editar") ou uma seta quando a linha abre outra tela.
 * Sem [aoClicar], a linha é só informativa.
 *
 * @param icone          ícone à esquerda.
 * @param titulo         texto principal da linha.
 * @param valor          texto secundário embaixo do título (ex: o apelido atual).
 * @param textoDaAcao    texto na cor da marca à direita; quando existe, a seta não aparece.
 * @param aoClicar       torna a linha tocável; null deixa a linha só informativa.
 * @param corDeDestaque  cor do ícone e do título (ex: vermelho em "Excluir minha conta").
 */
@Composable
fun LinhaDaConta(
    icone: ImageVector,
    titulo: String,
    modifier: Modifier = Modifier,
    valor: String? = null,
    textoDaAcao: String? = null,
    aoClicar: (() -> Unit)? = null,
    corDeDestaque: Color? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            // Só vira tocável quando há ação; a linha inteira recebe o toque
            .then(if (aoClicar != null) Modifier.clickable(onClick = aoClicar) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icone,
            contentDescription = null, // decorativo: o título já descreve a linha
            modifier = Modifier.size(24.dp),
            tint = corDeDestaque ?: MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodyLarge,
                color = corDeDestaque ?: MaterialTheme.colorScheme.onSurface
            )
            if (valor != null) {
                Text(
                    text = valor,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        when {
            textoDaAcao != null -> {
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = textoDaAcao,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            aoClicar != null -> Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Linha fina entre duas linhas do mesmo cartão, alinhada ao texto (depois do ícone). */
@Composable
fun SeparadorDeLinhas() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 56.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

/** Texto explicativo pequeno dentro de um cartão, alinhado ao texto das linhas. */
@Composable
fun NotaDoCartao(texto: String) {
    Text(
        text = texto,
        modifier = Modifier.padding(start = 56.dp, end = 16.dp, bottom = 12.dp),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
