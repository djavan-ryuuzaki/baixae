package br.com.djavanfernando.socialvideodownloader.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
fun BibliotecaTela(
    dependencias: Dependencias,
    bibliotecaId: String,
    aoAbrirVideo: (String) -> Unit,
    aoVoltar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val repositorio = dependencias.repositorio
    val biblioteca by remember(bibliotecaId) {
        repositorio.observarBibliotecas().map { lista -> lista.firstOrNull { it.id == bibliotecaId } }
    }.collectAsStateWithLifecycle(null)
    val videos by remember(bibliotecaId) {
        repositorio.observarVideos().map { lista ->
            lista.filter { it.bibliotecaId == bibliotecaId }.sortedByDescending(Video::dataDoDownload)
        }
    }.collectAsStateWithLifecycle(emptyList())

    Column(modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(biblioteca?.let { it.plataforma?.let { p -> rotulo(p) } ?: it.nome }.orEmpty()) },
            navigationIcon = {
                IconButton(onClick = aoVoltar) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.voltar))
                }
            },
        )
        if (videos.isEmpty()) {
            Text(stringResource(R.string.biblioteca_vazia), Modifier.padding(16.dp))
        }
        LazyColumn {
            items(videos, key = Video::id) { video ->
                ItemDeVideo(video, Modifier.clickable { aoAbrirVideo(video.id) })
            }
        }
    }
}

@Composable
fun ItemDeVideo(video: Video, modifier: Modifier = Modifier, marcadores: @Composable () -> Unit = {}) {
    ListItem(
        modifier = modifier,
        leadingContent = { Miniatura(video.caminhoDoArquivo, Modifier.size(width = 96.dp, height = 54.dp)) },
        headlineContent = { Text(video.nome, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            Column {
                Text(video.autor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (video.legenda.isNotBlank()) Text(video.legenda, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${formatarDuracao(video.duracaoEmSegundos)} · ${formatarData(video.dataDoDownload)}")
                marcadores()
            }
        },
    )
}
