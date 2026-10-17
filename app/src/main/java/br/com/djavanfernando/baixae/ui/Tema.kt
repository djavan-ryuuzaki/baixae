package br.com.djavanfernando.baixae.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val Claro = lightColorScheme(
    primary = Color(0xFF4F46E5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary = Color(0xFFDB2777),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFCE7F3),
    onSecondaryContainer = Color(0xFF500724),
    tertiary = Color(0xFF0891B2),
    background = Color(0xFFF8F7FC),
    surface = Color(0xFFF8F7FC),
    surfaceVariant = Color(0xFFE9E7F3),
    surfaceContainer = Color(0xFFF0EEF8),
    surfaceContainerHigh = Color(0xFFEAE8F4),
    outline = Color(0xFF79768A),
)

private val Escuro = darkColorScheme(
    primary = Color(0xFFA5B4FC),
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = Color(0xFF3730A3),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFFF9A8D4),
    onSecondary = Color(0xFF500724),
    secondaryContainer = Color(0xFF9D174D),
    onSecondaryContainer = Color(0xFFFCE7F3),
    tertiary = Color(0xFF67E8F9),
    background = Color(0xFF0F0E17),
    surface = Color(0xFF0F0E17),
    surfaceVariant = Color(0xFF2A2838),
    surfaceContainer = Color(0xFF1A1925),
    surfaceContainerHigh = Color(0xFF24222F),
    outline = Color(0xFF938FA6),
)

/** Cantos generosos, no estilo Material 3 atual. */
private val Formas = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

/** Usa as cores do papel de parede (Android 12+) e, antes disso, a paleta própria do app. */
@Composable
fun Tema(conteudo: @Composable () -> Unit) {
    val escuro = isSystemInDarkTheme()
    val context = LocalContext.current
    val cores = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (escuro) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        escuro -> Escuro
        else -> Claro
    }
    MaterialTheme(colorScheme = cores, shapes = Formas, content = conteudo)
}
