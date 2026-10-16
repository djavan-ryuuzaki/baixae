package br.com.djavanfernando.socialvideodownloader.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import br.com.djavanfernando.socialvideodownloader.core.EstadoNaFila
import br.com.djavanfernando.socialvideodownloader.core.ItemDaFila
import br.com.djavanfernando.socialvideodownloader.core.LinkNaoSuportado
import br.com.djavanfernando.socialvideodownloader.core.Plataforma
import br.com.djavanfernando.socialvideodownloader.fila.AgendadorDaFila
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface Aviso {
    data object AdicionadoAFila : Aviso
    data object LinkRecusado : Aviso
}

class BaixarViewModel(
    dependencias: Dependencias,
    private val iniciarFila: () -> Unit,
) : ViewModel() {
    private val fila = dependencias.fila
    val link = MutableStateFlow("")

    /** Pasta de destino escolhida só para o próximo Download; `null` usa a padrão. */
    val pastaDoProximo = MutableStateFlow<String?>(null)
    val pastaPadrao = dependencias.preferencias.observar().map { it.pastaPadrao }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")
    val aviso = MutableStateFlow<Aviso?>(null)
    val itens = dependencias.repositorio.observarFila()
        .map { lista -> lista.sortedByDescending(ItemDaFila::criadoEm) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val progresso = fila.progresso

    fun baixar() {
        val texto = link.value.trim()
        if (texto.isEmpty()) return
        val alvo = Plataforma.extrairLink(texto) ?: texto
        viewModelScope.launch {
            aviso.value = try {
                fila.enfileirar(alvo, pastaDoProximo.value)
                iniciarFila()
                link.value = ""
                pastaDoProximo.value = null
                Aviso.AdicionadoAFila
            } catch (_: LinkNaoSuportado) {
                Aviso.LinkRecusado
            }
        }
    }

    fun cancelar(id: String) = viewModelScope.launch { fila.cancelar(id) }

    fun remover(id: String) = viewModelScope.launch { fila.remover(id) }
}

@Composable
fun BaixarTela(dependencias: Dependencias, navegacao: Navegacao, modifier: Modifier = Modifier) {
    val context = LocalContext.current.applicationContext
    val viewModel = viewModel { BaixarViewModel(dependencias) { AgendadorDaFila.iniciar(context) } }
    val link by viewModel.link.collectAsStateWithLifecycle()
    val aviso by viewModel.aviso.collectAsStateWithLifecycle()
    val itens by viewModel.itens.collectAsStateWithLifecycle()
    val progresso by viewModel.progresso.collectAsStateWithLifecycle()
    val pastaDoProximo by viewModel.pastaDoProximo.collectAsStateWithLifecycle()
    val pastaPadrao by viewModel.pastaPadrao.collectAsStateWithLifecycle()
    var trocandoPasta by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = link,
            onValueChange = {
                viewModel.link.value = it
                viewModel.aviso.value = null
            },
            label = { Text(stringResource(R.string.baixar_link_rotulo)) },
            supportingText = { Text(stringResource(R.string.plataformas_suportadas, plataformasSuportadas())) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.baixar_pasta, pastaDoProximo ?: pastaPadrao),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = { trocandoPasta = true }) { Text(stringResource(R.string.baixar_trocar_pasta)) }
        }
        if (trocandoPasta) {
            DialogoDePasta(
                titulo = stringResource(R.string.baixar_pasta_so_deste),
                pastaAtual = pastaDoProximo ?: pastaPadrao,
                aoEscolher = { viewModel.pastaDoProximo.value = it },
                aoDispensar = { trocandoPasta = false },
            )
        }
        Button(onClick = viewModel::baixar, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.baixar_botao))
        }
        when (aviso) {
            Aviso.AdicionadoAFila -> Text(stringResource(R.string.adicionado_a_fila))
            Aviso.LinkRecusado -> Text(
                stringResource(
                    R.string.link_nao_suportado,
                    stringResource(R.string.plataformas_suportadas, plataformasSuportadas()),
                ),
                color = MaterialTheme.colorScheme.error,
            )
            null -> Unit
        }
        Text(stringResource(R.string.fila_titulo), style = MaterialTheme.typography.titleMedium)
        if (itens.isEmpty()) Text(stringResource(R.string.fila_vazia))
        LazyColumn {
            items(itens, key = ItemDaFila::id) { item ->
                ItemDaFilaLinha(
                    item = item,
                    progresso = progresso[item.id],
                    aoAbrir = { item.videoIds.firstOrNull()?.let(navegacao::abrirVideo) },
                    aoCancelar = { viewModel.cancelar(item.id) },
                    aoRemover = { viewModel.remover(item.id) },
                )
            }
        }
    }
}

@Composable
private fun ItemDaFilaLinha(
    item: ItemDaFila,
    progresso: Float?,
    aoAbrir: () -> Unit,
    aoCancelar: () -> Unit,
    aoRemover: () -> Unit,
) {
    ListItem(
        modifier = if (item.estado == EstadoNaFila.CONCLUIDO) Modifier.clickable(onClick = aoAbrir) else Modifier,
        overlineContent = { Text(rotulo(item.plataforma)) },
        headlineContent = { Text(item.titulo ?: item.link, maxLines = 2, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    descricaoDoEstado(item),
                    color = if (item.estado == EstadoNaFila.FALHOU) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                if (item.estado == EstadoNaFila.EM_ANDAMENTO) {
                    if (progresso == null) {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    } else {
                        LinearProgressIndicator(progress = { progresso }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        trailingContent = {
            if (item.estado.encerrado) {
                IconButton(onClick = aoRemover) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.fila_remover))
                }
            } else {
                IconButton(onClick = aoCancelar) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.fila_cancelar))
                }
            }
        },
    )
}

@Composable
fun descricaoDoEstado(item: ItemDaFila): String = when (item.estado) {
    EstadoNaFila.PENDENTE -> stringResource(R.string.estado_pendente)
    EstadoNaFila.AGUARDANDO_REDE -> stringResource(R.string.estado_aguardando_rede)
    EstadoNaFila.EM_ANDAMENTO -> stringResource(R.string.estado_em_andamento)
    EstadoNaFila.CONCLUIDO -> stringResource(R.string.estado_concluido)
    EstadoNaFila.FALHOU -> stringResource(R.string.estado_falhou)
    EstadoNaFila.CANCELADO -> stringResource(R.string.estado_cancelado)
}
