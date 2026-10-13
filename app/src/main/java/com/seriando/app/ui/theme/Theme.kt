package com.seriando.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Esquema de cores do tema ESCURO (principal do app), a partir da paleta Velvet Purple.
 * O texto sobre a cor principal é o Midnight, e não branco: o contraste fica em cerca de
 * 6,6:1, contra 2,7:1 do branco sobre #A98AF5.
 * Os papéis que a marca não define (containers, contornos, erro) foram derivados da paleta.
 */
private val EsquemaEscuro = darkColorScheme(
    primary = VelvetPurple,
    onPrimary = Midnight,
    primaryContainer = Color(0xFF3B2D66),
    onPrimaryContainer = Color(0xFFE8DEFF),
    secondary = Color(0xFFCBBDF0),
    onSecondary = Midnight,
    secondaryContainer = Color(0xFF3A3450),
    onSecondaryContainer = Color(0xFFE6E0F2),
    tertiary = Color(0xFFF0A6CA),
    onTertiary = Midnight,
    tertiaryContainer = Color(0xFF4F2B41),
    onTertiaryContainer = Color(0xFFFFD8EA),
    background = Midnight,
    onBackground = TextoPrincipalEscuro,
    surface = Midnight,
    onSurface = TextoPrincipalEscuro,
    surfaceVariant = VelvetSurface,                // cartões e campos
    onSurfaceVariant = TextoSecundarioEscuro,      // texto secundário
    surfaceTint = VelvetPurple,
    inverseSurface = TextoPrincipalEscuro,
    inverseOnSurface = Midnight,
    inversePrimary = PrimariaClara,
    outline = Color(0xFF8F889E),
    outlineVariant = Color(0xFF3E384B),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    scrim = Color.Black,
    // Níveis de superfície, do mais fundo ao mais alto; o Card usa o mais alto (Velvet Surface)
    surfaceDim = Midnight,
    surfaceBright = Color(0xFF3A3446),
    surfaceContainerLowest = Color(0xFF120F18),
    surfaceContainerLow = Color(0xFF1D1A26),
    surfaceContainer = Color(0xFF211D2B),          // barra de navegação
    surfaceContainerHigh = Color(0xFF252030),
    surfaceContainerHighest = VelvetSurface
)

/** Esquema de cores do tema CLARO, derivado da identidade da marca. */
private val EsquemaClaro = lightColorScheme(
    primary = PrimariaClara,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE9DDFF),
    onPrimaryContainer = Color(0xFF25005A),
    secondary = Color(0xFF63597C),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF8A4B67),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD8E8),
    onTertiaryContainer = Color(0xFF3A0B26),
    background = FundoClaro,
    onBackground = TextoPrincipalClaro,
    surface = FundoClaro,
    onSurface = TextoPrincipalClaro,
    surfaceVariant = SuperficieClara,              // cartões e campos
    onSurfaceVariant = TextoSecundarioClaro,       // texto secundário
    surfaceTint = PrimariaClara,
    inverseSurface = Color(0xFF322F3D),
    inverseOnSurface = Color(0xFFF5F0FF),
    inversePrimary = VelvetPurple,
    outline = Color(0xFF7D7690),
    outlineVariant = Color(0xFFCDC6DB),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    scrim = Color.Black,
    surfaceDim = Color(0xFFDDD8E6),
    surfaceBright = FundoClaro,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF6F2FD),
    surfaceContainer = Color(0xFFF1ECFA),          // barra de navegação
    surfaceContainerHigh = Color(0xFFEDE7F7),
    surfaceContainerHighest = SuperficieClara
)

/**
 * Tema do Seriando. Segue a configuração do aparelho: escuro quando o celular
 * está no modo escuro, claro nos demais casos. O escuro é o tema principal da marca.
 * As cores dinâmicas do Android 12+ (tiradas do papel de parede) foram removidas, porque
 * trocariam o roxo da marca pelas cores do usuário.
 */
@Composable
fun SeriandoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaEscuro else EsquemaClaro,
        typography = Typography, // a tipografia continua a de Type.kt
        content = content
    )
}