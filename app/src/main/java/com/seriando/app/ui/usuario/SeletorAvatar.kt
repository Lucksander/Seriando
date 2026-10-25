package com.seriando.app.ui.usuario

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import com.seriando.app.data.local.FotoDePerfilLocal
import kotlinx.coroutines.launch
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/**
 * Folha inferior para escolher o avatar (RF15): uma foto da galeria que fica só no aparelho (RF30),
 * a foto da conta Google (quando existe) e as 15 fotos do app. Os emojis das versões anteriores foram removidos.
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Foto do aparelho (RF30): a versão muda ao salvar ou remover, o que refaz a checagem abaixo
    val versaoDaFoto by FotoDePerfilLocal.versao.collectAsState()
    val temFotoLocal = remember(versaoDaFoto) { FotoDePerfilLocal.existente(context) != null }
    // true enquanto a foto escolhida está sendo recortada e salva
    var processando by remember { mutableStateOf(false) }

    // Photo Picker do Android: abre a galeria sem pedir nenhuma permissão e devolve só a imagem
    // escolhida (uri = null quando o usuário cancela)
    val seletorDeFoto = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                processando = true
                val salvou = FotoDePerfilLocal.salvarDe(context, uri)
                processando = false
                if (salvou) {
                    aoFechar()
                } else {
                    Toast.makeText(context, "Não foi possível usar essa imagem.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Escolher um avatar do app ou a foto do Google substitui a foto do aparelho: ela é apagada
    val escolher: (String) -> Unit = { id ->
        if (temFotoLocal) FotoDePerfilLocal.remover(context)
        aoSelecionar(id)
        aoFechar()
    }

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

            // Foto da galeria (RF30): fica só neste aparelho
            OpcaoFotoDoAparelho(
                temFoto = temFotoLocal,
                apelido = apelido,
                processando = processando,
                aoEscolher = {
                    seletorDeFoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                aoRemover = { FotoDePerfilLocal.remover(context) }
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Opção da foto da conta Google, só quando a conta tem foto
            if (fotoGoogle != null) {
                OpcaoFotoDoGoogle(
                    // Com foto do aparelho em uso, nenhuma outra opção aparece marcada
                    selecionado = !temFotoLocal && avatarSelecionado == AVATAR_GOOGLE,
                    apelido = apelido,
                    fotoGoogle = fotoGoogle,
                    aoClicar = { escolher(AVATAR_GOOGLE) }
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
                        selecionado = !temFotoLocal && id == avatarSelecionado,
                        apelido = apelido,
                        aoClicar = { escolher(id) }
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

/**
 * Linha "Foto do aparelho" no topo do seletor (RF30). Tocar nela abre a galeria; com uma foto já
 * escolhida, a linha fica destacada, mostra a foto e ganha o botão "Remover".
 *
 * @param temFoto     se já existe uma foto do aparelho em uso.
 * @param apelido     usado na inicial do avatar, caso a imagem não possa ser lida.
 * @param processando true enquanto a foto escolhida está sendo salva (mostra um indicador).
 * @param aoEscolher  abre o Photo Picker.
 * @param aoRemover   apaga a foto do aparelho.
 */
@Composable
private fun OpcaoFotoDoAparelho(
    temFoto: Boolean,
    apelido: String,
    processando: Boolean,
    aoEscolher: () -> Unit,
    aoRemover: () -> Unit
) {
    val forma = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            // Com foto em uso, a linha ganha o destaque da cor principal (como o item escolhido)
            .background(
                if (temFoto) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .then(
                if (temFoto) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, forma)
                else Modifier
            )
            // Sem clique enquanto salva, para não abrir o seletor duas vezes
            .selectable(selected = temFoto, enabled = !processando, onClick = aoEscolher, role = Role.Button)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (temFoto) {
            // Prévia da foto escolhida
            AvatarDoUsuario(
                avatarId = "",
                apelido = apelido,
                fotoGoogle = null,
                modifier = Modifier.size(48.dp),
                usarFotoLocal = true
            )
        } else {
            // Ainda sem foto: ícone de "adicionar imagem"
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AddPhotoAlternate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (temFoto) "Trocar foto do aparelho" else "Usar uma foto da galeria",
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = "Fica só neste aparelho: os outros usuários não a veem.",
                style = MaterialTheme.typography.bodySmall,
                color = if (temFoto) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        when {
            processando -> CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            temFoto -> TextButton(onClick = aoRemover) { Text("Remover") }
        }
    }
}
