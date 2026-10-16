package br.com.djavanfernando.socialvideodownloader.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.djavanfernando.socialvideodownloader.Dependencias
import br.com.djavanfernando.socialvideodownloader.R
import br.com.djavanfernando.socialvideodownloader.core.Biblioteca
import br.com.djavanfernando.socialvideodownloader.core.BibliotecaJaExiste
import br.com.djavanfernando.socialvideodownloader.core.NomeInvalido
import br.com.djavanfernando.socialvideodownloader.core.Plataforma
import br.com.djavanfernando.socialvideodownloader.core.Video
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BibliotecaViewModel(private val dependencias: Dependencias, private val bibliotecaId: String) : ViewModel() {
    private val repositorio = dependencias.repositorio
    private val acervo = dependencias.acervo

    val filtro = MutableStateFlow<Plataforma?>(null)
    val selecionados = MutableStateFlow<Set<String>>(emptySet())

    val biblioteca = repositorio.observarBibliotecas()
        .map { lista -> lista.firstOrNull { it.id == bibliotecaId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val outrasBibliotecas = repositorio.observarBibliotecas()
        .map { lista -> ordenarBibliotecas(lista.filter { it.id != bibliotecaId }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val todosDaBiblioteca = repositorio.observarVideos().map { lista ->
        lista.filter { it.bibliotecaId == bibliotecaId }
    }

    /** Plataformas presentes nesta Biblioteca, para os filtros. */
    val plataformas = todosDaBiblioteca.map { lista -> lista.map { it.plataforma }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val videos = combine(todosDaBiblioteca, filtro) { lista, plataforma ->
        lista.filter { plataforma == null || it.plataforma == plataforma }.sortedByDescending(Video::dataDoDownload)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun verificarArquivos() = viewModelScope.launch { acervo.verificarArquivos(bibliotecaId) }

    fun alternarSelecao(id: String) = selecionados.update { if (id in it) it - id else it + id }

    fun limparSelecao() {
        selecionados.value = emptySet()
    }

    fun moverSelecionados(destino: String) = viewModelScope.launch {
        acervo.mover(selecionados.value, destino)
        limparSelecao()
    }

    suspend fun renomear(nome: String) {
        acervo.renomearBiblioteca(bibliotecaId, nome)
    }

    fun apagar(depois: () -> Unit) = viewModelScope.launch {
        acervo.apagarBiblioteca(bibliotecaId)
        depois()
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BibliotecaTela(
    dependencias: Dependencias,
    bibliotecaId: String,
    aoAbrirVideo: (String) -> Unit,
    aoVoltar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = viewModel(key = "biblioteca-$bibliotecaId") { BibliotecaViewModel(dependencias, bibliotecaId) }
    val biblioteca by viewModel.biblioteca.collectAsStateWithLifecycle()
    val videos by viewModel.videos.collectAsStateWithLifecycle()
    val plataformas by viewModel.plataformas.collectAsStateWithLifecycle()
    val filtro by viewModel.filtro.collectAsStateWithLifecycle()
    val selecionados by viewModel.selecionados.collectAsStateWithLifecycle()
    val outras by viewModel.outrasBibliotecas.collectAsStateWithLifecycle()
    var renomeando by rememberSaveable { mutableStateOf(false) }
    var apagando by rememberSaveable { mutableStateOf(false) }
    var movendo by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = selecionados.isNotEmpty(), onBack = viewModel::limparSelecao)
    LaunchedEffect(bibliotecaId) { viewModel.verificarArquivos() }

    Column(modifier.fillMaxSize()) {
        if (selecionados.isEmpty()) {
            TopAppBar(
                title = { Text(biblioteca?.let { nomeDaBiblioteca(it) }.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = aoVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.voltar))
                    }
                },
                actions = {
                    if (biblioteca?.padrao == false) {
                        IconButton(onClick = { renomeando = true }) {
                            Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.biblioteca_renomear))
                        }
                        IconButton(onClick = { apagando = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.biblioteca_apagar))
                        }
                    }
                },
            )
        } else {
            TopAppBar(
                title = { Text(pluralStringResource(R.plurals.selecionados, selecionados.size, selecionados.size)) },
                navigationIcon = {
                    IconButton(onClick = viewModel::limparSelecao) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cancelar))
                    }
                },
                actions = {
                    IconButton(onClick = { movendo = true }) {
                        Icon(Icons.AutoMirrored.Filled.DriveFileMove, contentDescription = stringResource(R.string.mover))
                    }
                },
            )
        }
        if (plataformas.size > 1 || filtro != null) {
            Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(selected = filtro == null, onClick = { viewModel.filtro.value = null }, label = { Text(stringResource(R.string.filtro_todas)) })
                plataformas.forEach { plataforma ->
                    FilterChip(
                        selected = filtro == plataforma,
                        onClick = { viewModel.filtro.value = plataforma },
                        label = { Text(plataforma.nome) },
                    )
                }
            }
        }
        if (videos.isEmpty()) {
            Text(stringResource(R.string.biblioteca_vazia), Modifier.padding(16.dp))
        }
        LazyColumn {
            items(videos, key = Video::id) { video ->
                val selecionado = video.id in selecionados
                ItemDeVideo(
                    video,
                    Modifier
                        .combinedClickable(
                            onClick = {
                                if (selecionados.isEmpty()) aoAbrirVideo(video.id) else viewModel.alternarSelecao(video.id)
                            },
                            onLongClick = { viewModel.alternarSelecao(video.id) },
                        )
                        .then(if (selecionado) Modifier.background(MaterialTheme.colorScheme.secondaryContainer) else Modifier),
                ) {
                    if (video.arquivoAusente) {
                        Text(stringResource(R.string.arquivo_ausente), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    if (renomeando) {
        val jaExiste = stringResource(R.string.erro_biblioteca_ja_existe)
        val invalido = stringResource(R.string.erro_nome_invalido)
        DialogoDeTexto(
            titulo = stringResource(R.string.biblioteca_renomear),
            valorInicial = biblioteca?.nome.orEmpty(),
            rotulo = stringResource(R.string.video_nome),
            aoConfirmar = { nome ->
                try {
                    viewModel.renomear(nome)
                    null
                } catch (_: BibliotecaJaExiste) {
                    jaExiste
                } catch (_: NomeInvalido) {
                    invalido
                }
            },
            aoDispensar = { renomeando = false },
        )
    }
    if (apagando) {
        AlertDialog(
            onDismissRequest = { apagando = false },
            title = { Text(stringResource(R.string.biblioteca_apagar_titulo, biblioteca?.nome.orEmpty())) },
            text = { Text(stringResource(R.string.biblioteca_apagar_texto)) },
            confirmButton = {
                TextButton(onClick = {
                    apagando = false
                    viewModel.apagar(aoVoltar)
                }) { Text(stringResource(R.string.biblioteca_apagar)) }
            },
            dismissButton = { TextButton(onClick = { apagando = false }) { Text(stringResource(R.string.cancelar)) } },
        )
    }
    if (movendo) {
        AlertDialog(
            onDismissRequest = { movendo = false },
            title = { Text(stringResource(R.string.mover_para)) },
            text = {
                LazyColumn {
                    items(outras, key = Biblioteca::id) { destino ->
                        ListItem(
                            headlineContent = { Text(nomeDaBiblioteca(destino)) },
                            modifier = Modifier.clickable {
                                movendo = false
                                viewModel.moverSelecionados(destino.id)
                            },
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { movendo = false }) { Text(stringResource(R.string.cancelar)) } },
        )
    }
}

@Composable
fun nomeDaBiblioteca(biblioteca: Biblioteca): String = biblioteca.plataforma?.let { rotulo(it) } ?: biblioteca.nome

@Composable
fun ItemDeVideo(video: Video, modifier: Modifier = Modifier, marcadores: @Composable () -> Unit = {}) {
    ListItem(
        modifier = modifier,
        leadingContent = { Miniatura(video.caminhoDoArquivo, Modifier.size(width = 96.dp, height = 54.dp)) },
        headlineContent = { Text(video.nome, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            Column {
                Text("${video.plataforma.nome} · ${video.autor}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (video.legenda.isNotBlank()) Text(video.legenda, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${formatarDuracao(video.duracaoEmSegundos)} · ${formatarData(video.dataDoDownload)}")
                marcadores()
            }
        },
    )
}
