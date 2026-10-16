package br.com.djavanfernando.socialvideodownloader.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.djavanfernando.socialvideodownloader.Dependencias
import br.com.djavanfernando.socialvideodownloader.R
import br.com.djavanfernando.socialvideodownloader.core.NomeInvalido
import br.com.djavanfernando.socialvideodownloader.core.NomeJaExiste
import br.com.djavanfernando.socialvideodownloader.core.Video
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VideoViewModel(dependencias: Dependencias, private val videoId: String) : ViewModel() {
    private val acervo = dependencias.acervo

    val video = dependencias.repositorio.observarVideos()
        .map { lista -> lista.firstOrNull { it.id == videoId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun verificarArquivo() = viewModelScope.launch {
        acervo.video(videoId)?.let { acervo.verificarArquivos(it.bibliotecaId) }
    }

    suspend fun renomear(nome: String) {
        acervo.renomear(videoId, nome)
    }

    fun apagar(apagarArquivo: Boolean, depois: () -> Unit) = viewModelScope.launch {
        acervo.apagarVideo(videoId, apagarArquivo)
        depois()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoTela(dependencias: Dependencias, videoId: String, aoVoltar: () -> Unit) {
    val viewModel = viewModel(key = "video-$videoId") { VideoViewModel(dependencias, videoId) }
    val video by viewModel.video.collectAsStateWithLifecycle()
    var renomeando by rememberSaveable { mutableStateOf(false) }
    var apagando by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(videoId) { viewModel.verificarArquivo() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(video?.nome.orEmpty(), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = aoVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.voltar))
                    }
                },
                actions = {
                    if (video?.arquivoAusente == false) {
                        IconButton(onClick = { renomeando = true }) {
                            Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.video_renomear))
                        }
                    }
                    IconButton(onClick = { apagando = true }, enabled = video != null) {
                        Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.video_apagar))
                    }
                },
            )
        },
    ) { margens ->
        val atual = video ?: return@Scaffold
        DetalhesDoVideo(
            atual,
            aoRemover = { viewModel.apagar(apagarArquivo = false, depois = aoVoltar) },
            modifier = Modifier.padding(margens),
        )
        if (renomeando) {
            val nomeJaExiste = stringResource(R.string.erro_nome_ja_existe)
            val nomeInvalido = stringResource(R.string.erro_nome_invalido)
            DialogoDeTexto(
                titulo = stringResource(R.string.video_renomear),
                valorInicial = atual.nome,
                rotulo = stringResource(R.string.video_nome),
                aoConfirmar = { nome ->
                    try {
                        viewModel.renomear(nome)
                        null
                    } catch (_: NomeJaExiste) {
                        nomeJaExiste
                    } catch (_: NomeInvalido) {
                        nomeInvalido
                    } catch (erro: Exception) {
                        erro.message ?: nomeInvalido
                    }
                },
                aoDispensar = { renomeando = false },
            )
        }
        if (apagando) {
            DialogoDeApagar(
                video = atual,
                aoConfirmar = { apagarArquivo ->
                    apagando = false
                    viewModel.apagar(apagarArquivo, depois = aoVoltar)
                },
                aoDispensar = { apagando = false },
            )
        }
    }
}

@Composable
private fun DialogoDeApagar(video: Video, aoConfirmar: (Boolean) -> Unit, aoDispensar: () -> Unit) {
    var apagarArquivo by rememberSaveable { mutableStateOf(true) }
    AlertDialog(
        onDismissRequest = aoDispensar,
        title = { Text(stringResource(R.string.video_apagar_titulo)) },
        text = {
            Column {
                Text(video.nome)
                if (!video.arquivoAusente) {
                    Row(
                        Modifier.fillMaxWidth().clickable { apagarArquivo = !apagarArquivo },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = apagarArquivo, onCheckedChange = { apagarArquivo = it })
                        Text(stringResource(R.string.video_apagar_arquivo))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { aoConfirmar(apagarArquivo && !video.arquivoAusente) }) {
                Text(stringResource(R.string.video_apagar))
            }
        },
        dismissButton = { TextButton(onClick = aoDispensar) { Text(stringResource(R.string.cancelar)) } },
    )
}

@Composable
private fun DetalhesDoVideo(video: Video, aoRemover: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (video.arquivoAusente) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.arquivo_ausente), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.arquivo_ausente_texto))
                    Button(onClick = aoRemover) { Text(stringResource(R.string.arquivo_ausente_remover)) }
                }
            }
        } else {
            Miniatura(
                video.caminhoDoArquivo,
                Modifier.fillMaxWidth().aspectRatio(16f / 9f).clickable { context.reproduzir(video) },
            )
            Button(onClick = { context.reproduzir(video) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Text(stringResource(R.string.video_reproduzir))
            }
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
        Campo(stringResource(R.string.video_pasta), video.pasta)
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
