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

/**
 * Esquema de cores do tema CLARO, derivado da identidade da marca.
 * Lilás mais vivo: primária saturada, fundo com toque de lilás e cartões brancos por cima
 * (o cartão é mais claro que o fundo, ao contrário do que era antes).
 */
private val EsquemaClaro = lightColorScheme(
    primary = PrimariaClara,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2D3FF),          // áreas grandes com lilás visível
    onPrimaryContainer = Color(0xFF25005A),
    secondary = Color(0xFF6B4FB0),                 // roxo médio saturado (antes cinza-arroxeado)
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6DAFB),
    onSecondaryContainer = Color(0xFF1E1233),
    tertiary = Color(0xFFC2185B),                  // rosa vivo que combina com o lilás
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD9E8),
    onTertiaryContainer = Color(0xFF3E0420),
    background = FundoClaro,
    onBackground = TextoPrincipalClaro,
    surface = FundoClaro,
    onSurface = TextoPrincipalClaro,
    surfaceVariant = SuperficieClara,              // cartões e campos (brancos)
    onSurfaceVariant = TextoSecundarioClaro,       // texto secundário
    surfaceTint = PrimariaClara,
    inverseSurface = Color(0xFF2B1F45),
    inverseOnSurface = Color(0xFFF5EEFF),
    inversePrimary = VelvetPurple,
    outline = Color(0xFF7D6DA3),
    outlineVariant = Color(0xFFD9CCF2),            // contorno lilás fino dos cartões brancos
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    scrim = Color.Black,
    surfaceDim = Color(0xFFE0D5F2),
    surfaceBright = FundoClaro,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF8F3FF),
    surfaceContainer = Color(0xFFEBE0FF),          // barra de navegação, com lilás visível
    surfaceContainerHigh = Color(0xFFE6DAFB),
    surfaceContainerHighest = SuperficieClara
)

/**
 * Tema do Seriando. Segue a configuração do aparelho: escuro quando o celular
 * está no modo escuro, claro nos demais casos, a não ser que o usuário escolha um modo fixo
 * na aba Conta (ModoDoTema, RF80): a MainActivity decide e passa o resultado em [darkTheme].
 * O escuro é o tema principal da marca.
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