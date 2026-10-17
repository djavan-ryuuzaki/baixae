package br.com.djavanfernando.baixae.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.djavanfernando.baixae.Dependencias
import br.com.djavanfernando.baixae.R
import br.com.djavanfernando.baixae.core.ArquivoAusente
import br.com.djavanfernando.baixae.core.NomeInvalido
import br.com.djavanfernando.baixae.core.NomeJaExiste
import br.com.djavanfernando.baixae.core.Video
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VideoViewModel(dependencias: Dependencias, private val videoId: String) : ViewModel() {
    private val acervo = dependencias.acervo
    private val compartilhamento = dependencias.compartilhamento
    private val versaoSemSom = dependencias.versaoSemSom

    val video = dependencias.repositorio.observarVideos()
        .map { lista -> lista.firstOrNull { it.id == videoId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun verificarArquivo() = viewModelScope.launch {
        acervo.video(videoId)?.let { acervo.verificarArquivos(it.bibliotecaId) }
    }

    suspend fun renomear(nome: String) {
        acervo.renomear(videoId, nome)
    }

    /** Progresso da geração da cópia com Marca d'água; `null` quando nada está sendo gerado. */
    val gerandoCopia = MutableStateFlow<Float?>(null)
    val falhaAoCompartilhar = MutableStateFlow(false)
    private var preparo: Job? = null

    /** Prepara o arquivo do Compartilhamento e o entrega a [enviar]; Arquivo ausente só atualiza a tela. */
    fun compartilhar(enviar: (String) -> Unit) {
        if (preparo?.isActive == true) return
        preparo = viewModelScope.launch {
            try {
                val arquivo = compartilhamento.preparar(videoId) { gerandoCopia.value = it }
                enviar(arquivo.caminho)
            } catch (_: ArquivoAusente) {
            } catch (erro: CancellationException) {
                throw erro
            } catch (_: Exception) {
                falhaAoCompartilhar.value = true
            } finally {
                gerandoCopia.value = null
            }
        }
    }

    /** `true` enquanto a Versão sem som é gerada. */
    val criandoSemSom = MutableStateFlow(false)

    fun criarVersaoSemSom(aoTerminar: (Boolean) -> Unit) {
        if (criandoSemSom.value) return
        criandoSemSom.value = true
        viewModelScope.launch {
            val deuCerto = try {
                versaoSemSom.criar(videoId)
                true
            } catch (erro: CancellationException) {
                throw erro
            } catch (_: Exception) {
                false
            } finally {
                criandoSemSom.value = false
            }
            aoTerminar(deuCerto)
        }
    }

    fun cancelarCompartilhamento() {
        preparo?.cancel()
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
    val gerandoCopia by viewModel.gerandoCopia.collectAsStateWithLifecycle()
    val falhaAoCompartilhar by viewModel.falhaAoCompartilhar.collectAsStateWithLifecycle()
    val criandoSemSom by viewModel.criandoSemSom.collectAsStateWithLifecycle()
    val semSomCriada = stringResource(R.string.sem_som_criada)
    val semSomFalhou = stringResource(R.string.sem_som_falhou)
    val context = LocalContext.current

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
            aoCompartilhar = { viewModel.compartilhar(context::compartilhar) },
            criandoSemSom = criandoSemSom,
            aoCriarSemSom = {
                viewModel.criarVersaoSemSom { deuCerto ->
                    Toast.makeText(context, if (deuCerto) semSomCriada else semSomFalhou, Toast.LENGTH_SHORT).show()
                }
            },
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
        gerandoCopia?.let { progresso ->
            AlertDialog(
                onDismissRequest = {},
                title = { Text(stringResource(R.string.marca_dagua_gerando)) },
                text = { LinearProgressIndicator(progress = { progresso }, modifier = Modifier.fillMaxWidth()) },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = viewModel::cancelarCompartilhamento) { Text(stringResource(R.string.cancelar)) }
                },
            )
        }
        if (falhaAoCompartilhar) {
            AlertDialog(
                onDismissRequest = { viewModel.falhaAoCompartilhar.value = false },
                title = { Text(stringResource(R.string.marca_dagua_falhou)) },
                confirmButton = {
                    TextButton(onClick = { viewModel.falhaAoCompartilhar.value = false }) {
                        Text(stringResource(R.string.confirmar))
                    }
                },
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
private fun DetalhesDoVideo(
    video: Video,
    aoRemover: () -> Unit,
    aoCompartilhar: () -> Unit,
    criandoSemSom: Boolean,
    aoCriarSemSom: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
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
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(MaterialTheme.shapes.large)
                    .clickable(onClickLabel = stringResource(R.string.video_reproduzir)) { context.reproduzir(video) },
            ) {
                Miniatura(video.caminhoDoArquivo, Modifier.matchParentSize())
                Box(
                    Modifier
                        .align(Alignment.Center)
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = stringResource(R.string.video_reproduzir),
                        tint = Color.White,
                        modifier = Modifier.size(40.dp),
                    )
                }
                SeloDeDuracao(video.duracaoEmSegundos, Modifier.align(Alignment.BottomEnd).padding(10.dp))
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SeloDePlataforma(video.plataforma)
            Text(video.nome, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(video.autor, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!video.arquivoAusente) {
                AcaoDoVideo(Icons.Filled.Share, stringResource(R.string.video_compartilhar), aoCompartilhar, Modifier.weight(1f))
                AcaoDoVideo(
                    Icons.AutoMirrored.Filled.VolumeOff,
                    stringResource(if (criandoSemSom) R.string.sem_som_criando else R.string.sem_som_criar),
                    aoCriarSemSom,
                    Modifier.weight(1f),
                    habilitada = !criandoSemSom,
                )
            }
            AcaoDoVideo(
                Icons.AutoMirrored.Filled.OpenInNew,
                stringResource(R.string.video_abrir_original),
                { context.abrirOriginal(video) },
                Modifier.weight(1f),
            )
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
            Column(Modifier.padding(vertical = 8.dp)) {
                Text(
                    stringResource(R.string.video_detalhes),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                if (video.legenda.isNotBlank()) Campo(Icons.AutoMirrored.Outlined.Notes, stringResource(R.string.video_legenda), video.legenda)
                Campo(Icons.Outlined.Timer, stringResource(R.string.video_duracao), formatarDuracao(video.duracaoEmSegundos))
                Campo(Icons.Outlined.Event, stringResource(R.string.video_data_do_download), formatarData(video.dataDoDownload))
                Campo(Icons.Outlined.Folder, stringResource(R.string.video_pasta), video.pasta)
                Campo(Icons.Outlined.Link, stringResource(R.string.video_link), video.link)
            }
        }
    }
}

/** Botão de ação com ícone em cima e rótulo embaixo. */
@Composable
private fun AcaoDoVideo(
    icone: ImageVector,
    rotulo: String,
    aoTocar: () -> Unit,
    modifier: Modifier = Modifier,
    habilitada: Boolean = true,
) {
    FilledTonalButton(
        onClick = aoTocar,
        enabled = habilitada,
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 12.dp),
        modifier = modifier.fillMaxHeight(),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icone, contentDescription = null)
            Text(rotulo, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center, maxLines = 2)
        }
    }
}

@Composable
private fun Campo(icone: ImageVector, rotulo: String, valor: String) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icone, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(Modifier.padding(start = 16.dp)) {
            Text(rotulo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(valor, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
