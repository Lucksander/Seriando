package com.seriando.app.ui.comum

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seriando.app.R

// Família da fonte do logotipo, lida do arquivo res/font/poppins_semibold.ttf.
private val FonteLogo = FontFamily(Font(R.font.poppins_semibold, FontWeight.SemiBold))

/**
 * Logotipo do Seriando: o ícone do S faz o papel da primeira letra e o resto do
 * nome ("eriando") vem ao lado, na fonte do logotipo.
 *
 * @param modifier      posicionamento e margens, definidos por quem usa.
 * @param alturaIcone   altura do ícone do S; a largura acompanha a proporção da imagem.
 * @param tamanhoTexto  tamanho da fonte de "eriando"; ajuste junto com alturaIcone
 *                      até as letras parecerem ter a mesma altura do S.
 */
@Composable
fun LogoSeriando(
    modifier: Modifier = Modifier,
    alturaIcone: Dp = 36.dp,
    tamanhoTexto: TextUnit = 28.sp
) {
    Row(
        // Leitores de tela devem ler o nome inteiro, e não "Seriando eriando"
        modifier = modifier.clearAndSetSemantics { contentDescription = "Seriando" },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // O S: contentDescription nulo porque a descrição fica no Row
        Image(
            painter = painterResource(R.drawable.logo_seriando),
            contentDescription = null,
            modifier = Modifier.height(alturaIcone)
        )
        // O resto do nome, na cor de texto do tema (claro ou escuro)
        Text(
            text = "eriando",
            fontFamily = FonteLogo,
            fontWeight = FontWeight.SemiBold,
            fontSize = tamanhoTexto,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}