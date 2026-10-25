package com.seriando.app.ui.usuario

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.seriando.app.data.local.FotoDePerfilLocal

// ---------------------------------------------------------------------------------------
// Avatares do app (RF15)
//
// O perfil guarda só um texto curto em avatarId (cabe no limite de 20 caracteres das regras
// do Firestore). Valores possíveis:
//   "google"  -> foto da conta Google;
//   "foto_01" até "foto_15" -> uma das 15 fotos do app;
//   qualquer outro valor (ex: um emoji de versões antigas) -> inicial do apelido.
// ---------------------------------------------------------------------------------------

/** avatarId que representa a foto da conta Google. */
const val AVATAR_GOOGLE = "google"

/** Quantas fotos de avatar o app oferece. */
const val TOTAL_AVATARES_FOTO = 15

/** Ids das fotos do app, na ordem em que aparecem no seletor: "foto_01" ... "foto_15". */
val AVATARES_DISPONIVEIS: List<String> =
    (1..TOTAL_AVATARES_FOTO).map { "foto_" + it.toString().padStart(2, '0') }

/**
 * Descobre o recurso (res/drawable-nodpi/avatar_NN.webp) de um avatar do app.
 * "foto_03" corresponde ao arquivo avatar_03.
 *
 * A busca é feita pelo nome, e não por R.drawable.avatar_03, para o app compilar e abrir
 * mesmo antes de as imagens serem adicionadas à pasta; sem o arquivo, a tela mostra a
 * inicial do apelido no lugar da foto.
 *
 * @return o id do recurso, ou 0 se o avatarId não for uma foto do app ou o arquivo não existir.
 */
@DrawableRes
fun recursoDoAvatar(context: Context, avatarId: String): Int {
    // Só ids da lista fixa viram recurso; qualquer outro texto (vindo da nuvem) é ignorado
    if (avatarId !in AVATARES_DISPONIVEIS) return 0
    val nomeDoArquivo = "avatar_" + avatarId.removePrefix("foto_")
    return context.resources.getIdentifier(nomeDoArquivo, "drawable", context.packageName)
}

/** Primeira letra ou número do apelido, em maiúscula; "?" se o apelido estiver vazio. */
private fun inicialDoApelido(apelido: String): String =
    apelido.trim().firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString() ?: "?"

/**
 * Avatar redondo de um usuário, usado na aba Conta, na Home e nas opiniões.
 * Concentra as regras de exibição para as telas ficarem iguais:
 *  - [usarFotoLocal] e foto escolhida neste aparelho (RF30): a foto do aparelho;
 *  - "google" com foto disponível: a foto da conta Google;
 *  - "foto_NN" com a imagem no app: a foto do app;
 *  - qualquer outro caso (emoji antigo, imagem ausente, sem foto do Google): a inicial do
 *    apelido sobre o fundo roxo da marca.
 *
 * O tamanho vem do [modifier] (ex: Modifier.size(44.dp)); o círculo é recortado aqui.
 *
 * @param avatarId   valor guardado no perfil.
 * @param apelido    usado para a inicial quando não há imagem.
 * @param fotoGoogle endereço da foto da conta Google, ou null quando não se tem (ex: outro autor).
 * @param descricao  texto para leitores de tela; null deixa o avatar decorativo.
 * @param usarFotoLocal true só nos avatares do PRÓPRIO usuário (Home, Conta, seletor): a foto do
 *                   aparelho tem prioridade sobre o avatarId. Nos avatares de outros usuários
 *                   fica false, porque a foto local nunca sai do aparelho.
 */
@Composable
fun AvatarDoUsuario(
    avatarId: String,
    apelido: String,
    fotoGoogle: Any?,
    modifier: Modifier = Modifier,
    descricao: String? = null,
    usarFotoLocal: Boolean = false
) {
    val context = LocalContext.current
    // Só recalcula quando o avatarId muda
    val recurso = remember(avatarId) { recursoDoAvatar(context, avatarId) }

    // Foto do aparelho (RF30): a versão muda quando a foto é trocada ou removida, o que refaz
    // a leitura do arquivo e atualiza o avatar na hora, em todas as telas
    val versaoDaFoto by FotoDePerfilLocal.versao.collectAsState()
    val fotoLocal = remember(usarFotoLocal, versaoDaFoto) {
        if (usarFotoLocal) FotoDePerfilLocal.existente(context) else null
    }

    BoxWithConstraints(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        when {
            // Foto do aparelho: tem prioridade. O Coil já considera a data de modificação do
            // arquivo, então uma foto nova substitui a antiga sem ficar presa no cache
            fotoLocal != null -> AsyncImage(
                model = fotoLocal,
                contentDescription = descricao,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            avatarId == AVATAR_GOOGLE && fotoGoogle != null -> AsyncImage(
                model = fotoGoogle,
                contentDescription = descricao,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            recurso != 0 -> Image(
                painter = painterResource(recurso),
                contentDescription = descricao,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            else -> {
                // A letra acompanha o tamanho do avatar (cerca de 45% do diâmetro)
                val lado = if (maxWidth.value.isFinite()) maxWidth.value else 40f
                Text(
                    text = inicialDoApelido(apelido),
                    fontSize = (lado * 0.45f).sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
