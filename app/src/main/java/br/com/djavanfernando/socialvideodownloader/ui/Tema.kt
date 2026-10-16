package br.com.djavanfernando.socialvideodownloader.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun Tema(conteudo: @Composable () -> Unit) {
    val cores = if (isSystemInDarkTheme()) {
        darkColorScheme(primary = Color(0xFF9DB5FF))
    } else {
        lightColorScheme(primary = Color(0xFF1E5EFF))
    }
    MaterialTheme(colorScheme = cores, content = conteudo)
}
