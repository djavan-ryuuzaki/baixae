package br.com.djavanfernando.socialvideodownloader.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.djavanfernando.socialvideodownloader.Dependencias
import br.com.djavanfernando.socialvideodownloader.R
import br.com.djavanfernando.socialvideodownloader.core.Video
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoTela(dependencias: Dependencias, videoId: String, aoVoltar: () -> Unit) {
    val video by remember(videoId) {
        dependencias.repositorio.observarVideos().map { lista -> lista.firstOrNull { it.id == videoId } }
    }.collectAsStateWithLifecycle(null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(video?.nome.orEmpty(), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = aoVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.voltar))
                    }
                },
            )
        },
    ) { margens ->
        val atual = video ?: return@Scaffold
        DetalhesDoVideo(atual, Modifier.padding(margens))
    }
}

@Composable
private fun DetalhesDoVideo(video: Video, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Miniatura(
            video.caminhoDoArquivo,
            Modifier.fillMaxWidth().aspectRatio(16f / 9f).clickable { context.reproduzir(video) },
        )
        Button(onClick = { context.reproduzir(video) }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null)
            Text(stringResource(R.string.video_reproduzir))
        }
        OutlinedButton(onClick = { context.abrirOriginal(video) }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
            Text(stringResource(R.string.video_abrir_original))
        }
        Campo(stringResource(R.string.video_plataforma), rotulo(video.plataforma))
        Campo(stringResource(R.string.video_autor), video.autor)
        if (video.legenda.isNotBlank()) Campo(stringResource(R.string.video_legenda), video.legenda)
        Campo(stringResource(R.string.video_duracao), formatarDuracao(video.duracaoEmSegundos))
        Campo(stringResource(R.string.video_data_do_download), formatarData(video.dataDoDownload))
        Campo(stringResource(R.string.video_link), video.link)
    }
}

@Composable
private fun Campo(rotulo: String, valor: String) {
    Column {
        Text(rotulo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(valor, style = MaterialTheme.typography.bodyLarge)
    }
}
