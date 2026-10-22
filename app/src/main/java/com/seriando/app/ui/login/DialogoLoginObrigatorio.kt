package com.seriando.app.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Folha inferior exibida quando o visitante tenta fazer algo que exige conta (RF54):
 * adicionar à lista, marcar episódio, avaliar, opinar, votar ou denunciar.
 * Mostra os benefícios de ter uma conta e oferece o login com Google.
 *
 * Mantém o nome e os parâmetros do antigo diálogo, então as telas que a chamam não mudam.
 *
 * @param aoFechar          chamado ao tocar em "Continuar navegando", deslizar a folha para baixo
 *                          ou tocar fora dela.
 * @param aoLogarComSucesso chamado após o login dar certo, para a tela que abriu a folha
 *                          continuar a ação que o usuário queria fazer.
 */
@OptIn(ExperimentalMaterial3Api::class) // ModalBottomSheet ainda é marcado como experimental
@Composable
fun DialogoLoginObrigatorio(
    aoFechar: () -> Unit,
    aoLogarComSucesso: () -> Unit
) {
    // Estado e ação do login, o mesmo usado pela tela de entrada (RF53)
    val login = rememberLoginComGoogle(aoLogarComSucesso = aoLogarComSucesso)

    // skipPartiallyExpanded: a folha abre inteira, sem parar na metade da altura
    val estadoFolha = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = aoFechar,
        sheetState = estadoFolha
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // Mantém o conteúdo acima da barra de navegação do sistema
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cadeado na cor da marca
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null, // decorativo: o título já descreve a folha
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Crie sua conta para continuar",
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Lista de benefícios (textos da especificação do Bloco L)
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Beneficio(
                    icone = Icons.Outlined.BookmarkBorder,
                    titulo = "Sua lista sincronizada",
                    descricao = "Salve seu progresso e acesse de qualquer lugar."
                )
                Beneficio(
                    icone = Icons.Outlined.StarBorder,
                    titulo = "Dê sua opinião",
                    descricao = "Avalie obras, marque emoções e interaja com a comunidade."
                )
                Beneficio(
                    icone = Icons.Outlined.CloudOff,
                    titulo = "Modo offline",
                    descricao = "Continue registrando seus episódios mesmo sem internet."
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Ação principal: login com Google (desabilitado enquanto o login está em andamento)
            Button(
                onClick = login.entrar,
                enabled = !login.entrando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(if (login.entrando) "Entrando..." else "Entrar com o Google")
            }

            // Ação secundária: fecha a folha e deixa o visitante seguir navegando
            TextButton(
                onClick = aoFechar,
                enabled = !login.entrando,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Text("Continuar navegando")
            }
        }
    }
}

// Uma linha da lista de benefícios: ícone à esquerda, título em destaque e descrição embaixo.
@Composable
private fun Beneficio(icone: ImageVector, titulo: String, descricao: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icone,
            contentDescription = null, // decorativo: o texto ao lado já diz tudo
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = descricao,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}