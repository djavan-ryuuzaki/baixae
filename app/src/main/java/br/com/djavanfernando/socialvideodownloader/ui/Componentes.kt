package br.com.djavanfernando.socialvideodownloader.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.djavanfernando.socialvideodownloader.core.Plataforma

/** Cor de marca de cada Plataforma, usada em selos e destaques. */
val Plataforma.cor: Color
    get() = when (this) {
        Plataforma.INSTAGRAM -> Color(0xFFE1306C)
        Plataforma.FACEBOOK -> Color(0xFF1877F2)
        Plataforma.X -> Color(0xFF52525B)
        Plataforma.BLUESKY -> Color(0xFF0085FF)
        Plataforma.YOUTUBE -> Color(0xFFFF0033)
    }

/** Selo em forma de pílula com o nome da Plataforma na cor dela. */
@Composable
fun SeloDePlataforma(plataforma: Plataforma, modifier: Modifier = Modifier) {
    Text(
        rotulo(plataforma),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = Color.White,
        modifier = modifier
            .clip(CircleShape)
            .background(plataforma.cor)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

/** Duração sobreposta no canto de uma miniatura. */
@Composable
fun SeloDeDuracao(segundos: Long, modifier: Modifier = Modifier) {
    Text(
        formatarDuracao(segundos),
        style = MaterialTheme.typography.labelSmall,
        color = Color.White,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.Black.copy(alpha = 0.65f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

/** Ícone em destaque com um texto, para listas vazias. */
@Composable
fun EstadoVazio(icone: ImageVector, titulo: String, modifier: Modifier = Modifier, dica: String? = null) {
    Column(
        modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            Modifier.size(72.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icone, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(36.dp))
        }
        Text(titulo, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        if (dica != null) {
            Text(
                dica,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Grupo de itens com título, no estilo das configurações do Android atual. */
@Composable
fun Secao(titulo: String, modifier: Modifier = Modifier, conteudo: @Composable () -> Unit) {
    Column(modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            titulo,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp),
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column { conteudo() }
        }
    }
}

/** Ícone dentro de um círculo tonal, para o início de itens de lista. */
@Composable
fun IconeRedondo(icone: ImageVector, modifier: Modifier = Modifier) {
    Box(
        modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icone, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(22.dp))
    }
}

/** Barra de topo das abas: título grande e ações, sem repetir a margem da barra de status que o Scaffold já aplica. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TituloDeTela(titulo: String, acoes: @Composable RowScope.() -> Unit = {}) {
    TopAppBar(
        title = { Text(titulo, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) },
        actions = acoes,
        windowInsets = WindowInsets(0),
    )
}
