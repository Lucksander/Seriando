package com.seriando.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import com.seriando.app.model.Emocao

/**
 * Cor de uma emoção adaptada ao tema atual (bolinha, borda e preenchimento dos chips).
 *
 * As cores do model (Emocao.cor) são tons escuros de Material (800), que funcionam bem no
 * tema claro mas ficam apagados sobre o fundo escuro. No tema escuro, a cor é clareada em 35%
 * (mistura com branco). Detecta o tema pelo fundo atual, e não por isSystemInDarkTheme(),
 * para continuar certo se um dia o usuário puder escolher o tema manualmente.
 */
@Composable
fun corDaEmocao(emocao: Emocao): Color {
    val base = Color(emocao.cor)
    val temaEscuro = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return if (temaEscuro) lerp(base, Color.White, 0.35f) else base
}