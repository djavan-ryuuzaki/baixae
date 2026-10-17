package br.com.djavanfernando.baixae.ui

import android.content.ClipboardManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.djavanfernando.baixae.R
import br.com.djavanfernando.baixae.core.LinkRepetido

/** Data do último clipe lido, no processo inteiro: um clipe já sugerido (ou dispensado) não volta a ser sugerido. */
private var dataDoUltimoClipeLido = -1L

/**
 * Lê a área de transferência só quando a janela do app está em foco e só se o conteúdo mudou desde a
 * última leitura (pela data do clipe), para não ler à toa nem repetir a mesma sugestão.
 */
@Composable
fun ObservarAreaDeTransferencia(aoMudar: (String?) -> Unit) {
    val context = LocalContext.current
    val emFoco = LocalWindowInfo.current.isWindowFocused
    LaunchedEffect(emFoco) {
        if (!emFoco) return@LaunchedEffect
        val gerenciador = context.getSystemService(ClipboardManager::class.java) ?: return@LaunchedEffect
        val descricao = gerenciador.primaryClipDescription ?: return@LaunchedEffect
        if (descricao.timestamp == dataDoUltimoClipeLido) return@LaunchedEffect
        dataDoUltimoClipeLido = descricao.timestamp
        aoMudar(gerenciador.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString())
    }
}

@Composable
fun SugestaoDeLinkCopiado(link: String, aoBaixar: () -> Unit, aoDispensar: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(R.string.sugestao_titulo), style = MaterialTheme.typography.titleSmall)
            Text(link, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = aoDispensar) { Text(stringResource(R.string.sugestao_dispensar)) }
                TextButton(onClick = aoBaixar) { Text(stringResource(R.string.baixar_botao)) }
            }
        }
    }
}

@Composable
fun DialogoDeLinkRepetido(
    anterior: LinkRepetido,
    aoAbrir: () -> Unit,
    aoBaixarDeNovo: () -> Unit,
    aoDispensar: () -> Unit,
) {
    val biblioteca = anterior.biblioteca.plataforma?.let { rotulo(it) } ?: anterior.biblioteca.nome
    AlertDialog(
        onDismissRequest = aoDispensar,
        title = { Text(stringResource(R.string.repetido_titulo)) },
        text = { Text(stringResource(R.string.repetido_texto, anterior.video.nome, biblioteca)) },
        confirmButton = { TextButton(onClick = aoAbrir) { Text(stringResource(R.string.repetido_abrir)) } },
        dismissButton = { TextButton(onClick = aoBaixarDeNovo) { Text(stringResource(R.string.repetido_baixar_de_novo)) } },
    )
}
