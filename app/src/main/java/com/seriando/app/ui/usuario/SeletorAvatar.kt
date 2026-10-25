package com.seriando.app.ui.usuario

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/**
 * Folha inferior para escolher o avatar (RF15): a foto da conta Google (quando existe) e as
 * 15 fotos do app. Os emojis das versões anteriores foram removidos.
 *
 * A escolha é gravada por quem chama, em [aoSelecionar]; a folha fecha logo depois.
 *
 * @param avatarSelecionado avatarId atual do perfil, para destacar a opção escolhida.
 * @param apelido           usado na inicial exibida quando uma imagem não está disponível.
 * @param fotoGoogle        foto da conta Google; null esconde a opção "Usar a foto do Google".
 * @param aoSelecionar      recebe o avatarId escolhido ("google" ou "foto_NN").
 * @param aoFechar          chamada ao fechar a folha (por toque fora, arrastar ou depois de escolher).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SeletorAvatar(
    avatarSelecionado: String,
    apelido: String,
    fotoGoogle: Any?,
    aoSelecionar: (String) -> Unit,
    aoFechar: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = aoFechar,
        // Abre já na altura total do conteúdo, sem parar no meio
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(text = "Escolha seu avatar", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            // Opção da foto da conta Google, só quando a conta tem foto
            if (fotoGoogle != null) {
                OpcaoFotoDoGoogle(
                    selecionado = avatarSelecionado == AVATAR_GOOGLE,
                    apelido = apelido,
                    fotoGoogle = fotoGoogle,
                    aoClicar = {
                        aoSelecionar(AVATAR_GOOGLE)
                        aoFechar()
                    }
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Grade com as 15 fotos: até 4 por linha, centralizadas
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                maxItemsInEachRow = 4
            ) {
                AVATARES_DISPONIVEIS.forEach { id ->
                    OpcaoDeAvatar(
                        avatarId = id,
                        selecionado = id == avatarSelecionado,
                        apelido = apelido,
                        aoClicar = {
                            aoSelecionar(id)
                            aoFechar()
                        }
                    )
                }
            }
        }
    }
}

/** Uma foto da grade. A escolhida ganha um anel na cor da marca e a foto encolhe um pouco dentro dele. */
@Composable
private fun OpcaoDeAvatar(
    avatarId: String,
    selecionado: Boolean,
    apelido: String,
    aoClicar: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            // selectable dá o toque, o ripple e a semântica de "item de escolha única"
            .selectable(selected = selecionado, onClick = aoClicar, role = Role.RadioButton)
            .then(
                if (selecionado) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                else Modifier
            )
            .padding(if (selecionado) 5.dp else 0.dp)
    ) {
        AvatarDoUsuario(
            avatarId = avatarId,
            apelido = apelido,
            fotoGoogle = null,
            modifier = Modifier.fillMaxSize(),
            descricao = "Avatar ${avatarId.removePrefix("foto_")}"
        )
    }
}

/** Linha com a foto da conta Google e um botão de rádio, acima da grade. */
@Composable
private fun OpcaoFotoDoGoogle(
    selecionado: Boolean,
    apelido: String,
    fotoGoogle: Any?,
    aoClicar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .selectable(selected = selecionado, onClick = aoClicar, role = Role.RadioButton)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarDoUsuario(
            avatarId = AVATAR_GOOGLE,
            apelido = apelido,
            fotoGoogle = fotoGoogle,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Usar a foto do Google", style = MaterialTheme.typography.titleSmall)
            Text(
                text = "A foto da sua conta Google",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // onClick = null: o toque é tratado pela linha inteira (selectable acima)
        RadioButton(selected = selecionado, onClick = null)
    }
}
