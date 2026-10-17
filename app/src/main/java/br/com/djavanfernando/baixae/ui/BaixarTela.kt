package br.com.djavanfernando.baixae.ui

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.djavanfernando.baixae.Dependencias
import br.com.djavanfernando.baixae.R
import br.com.djavanfernando.baixae.core.AcaoAposFalha
import br.com.djavanfernando.baixae.core.EstadoNaFila
import br.com.djavanfernando.baixae.core.ItemDaFila
import br.com.djavanfernando.baixae.core.LinkNaoSuportado
import br.com.djavanfernando.baixae.core.LinkRepetido
import br.com.djavanfernando.baixae.core.Plataforma
import br.com.djavanfernando.baixae.core.TipoDeFalha
import br.com.djavanfernando.baixae.fila.AgendadorDaFila
import kotlinx.coroutines.CancellationException
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
    private val acervo = dependencias.acervo
    private val extratores = dependencias.extratores
    val link = MutableStateFlow("")

    /** Pasta de destino escolhida só para o próximo Download; `null` usa a padrão. */
    val pastaDoProximo = MutableStateFlow<String?>(null)
    val pastaPadrao = dependencias.preferencias.observar().map { it.pastaPadrao }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")
    val aviso = MutableStateFlow<Aviso?>(null)

    /** Link copiado sugerido para Download ("Baixar o Link copiado?"). */
    val sugestao = MutableStateFlow<String?>(null)

    /** Pedido de Download de um Link já baixado, aguardando o usuário escolher Abrir ou Baixar de novo. */
    val repetido = MutableStateFlow<Pair<String, LinkRepetido>?>(null)
    val itens = dependencias.repositorio.observarFila()
        .map { lista -> lista.sortedByDescending(ItemDaFila::criadoEm) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val progresso = fila.progresso

    fun baixar() {
        val texto = link.value.trim()
        if (texto.isEmpty()) return
        pedir(Plataforma.extrairLink(texto) ?: texto)
    }

    fun baixarSugestao() {
        val alvo = sugestao.value ?: return
        sugestao.value = null
        pedir(alvo)
    }

    fun dispensarSugestao() {
        sugestao.value = null
    }

    /** Chamado com o texto da área de transferência quando ele muda e o app está em primeiro plano. */
    fun areaDeTransferenciaMudou(texto: String?) {
        sugestao.value = Plataforma.sugestaoDeDownload(texto)
    }

    fun baixarDeNovo() {
        val (alvo, _) = repetido.value ?: return
        repetido.value = null
        viewModelScope.launch { enfileirar(alvo) }
    }

    fun dispensarRepetido() {
        repetido.value = null
    }

    private fun pedir(alvo: String) {
        viewModelScope.launch {
            val anterior = acervo.downloadAnterior(alvo)
            if (anterior != null) {
                aviso.value = null
                repetido.value = alvo to anterior
            } else {
                enfileirar(alvo)
            }
        }
    }

    private suspend fun enfileirar(alvo: String) {
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

    fun cancelar(id: String) = viewModelScope.launch { fila.cancelar(id) }

    /** Itens cujos Extratores estão sendo atualizados a pedido do usuário. */
    val atualizando = MutableStateFlow<Set<String>>(emptySet())
    val falhaAoAtualizar = MutableStateFlow(false)

    fun tentarDeNovo(id: String) = viewModelScope.launch {
        fila.tentarDeNovo(id)
        iniciarFila()
    }

    fun atualizarExtratoresETentar(id: String) = viewModelScope.launch {
        atualizando.value += id
        try {
            extratores.atualizarETentarDeNovo(id)
            iniciarFila()
        } catch (erro: CancellationException) {
            throw erro
        } catch (_: Exception) {
            falhaAoAtualizar.value = true
        } finally {
            atualizando.value -= id
        }
    }

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
    val sugestao by viewModel.sugestao.collectAsStateWithLifecycle()
    val repetido by viewModel.repetido.collectAsStateWithLifecycle()
    val atualizando by viewModel.atualizando.collectAsStateWithLifecycle()
    val falhaAoAtualizar by viewModel.falhaAoAtualizar.collectAsStateWithLifecycle()
    if (falhaAoAtualizar) {
        AlertDialog(
            onDismissRequest = { viewModel.falhaAoAtualizar.value = false },
            title = { Text(stringResource(R.string.extratores_falha_ao_atualizar)) },
            confirmButton = {
                TextButton(onClick = { viewModel.falhaAoAtualizar.value = false }) { Text(stringResource(R.string.confirmar)) }
            },
        )
    }
    ObservarAreaDeTransferencia(viewModel::areaDeTransferenciaMudou)
    repetido?.let { (_, anterior) ->
        DialogoDeLinkRepetido(
            anterior = anterior,
            aoAbrir = {
                viewModel.dispensarRepetido()
                navegacao.abrirVideo(anterior.video.id)
            },
            aoBaixarDeNovo = viewModel::baixarDeNovo,
            aoDispensar = viewModel::dispensarRepetido,
        )
    }
    if (trocandoPasta) {
        DialogoDePasta(
            titulo = stringResource(R.string.baixar_pasta_so_deste),
            pastaAtual = pastaDoProximo ?: pastaPadrao,
            aoEscolher = { viewModel.pastaDoProximo.value = it },
            aoDispensar = { trocandoPasta = false },
        )
    }

    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        sugestao?.let { copiado ->
            item(key = "sugestao") {
                SugestaoDeLinkCopiado(copiado, aoBaixar = viewModel::baixarSugestao, aoDispensar = viewModel::dispensarSugestao)
            }
        }
        item(key = "pedido") {
            CartaoDePedido(
                link = link,
                aoMudarLink = {
                    viewModel.link.value = it
                    viewModel.aviso.value = null
                },
                pasta = pastaDoProximo ?: pastaPadrao,
                aoTrocarPasta = { trocandoPasta = true },
                aviso = aviso,
                aoBaixar = viewModel::baixar,
            )
        }
        item(key = "titulo-da-fila") {
            Text(
                stringResource(R.string.fila_titulo),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp, start = 4.dp),
            )
        }
        if (itens.isEmpty()) {
            item(key = "fila-vazia") {
                EstadoVazio(
                    Icons.Outlined.CloudDownload,
                    stringResource(R.string.fila_vazia),
                    dica = stringResource(R.string.fila_vazia_dica),
                )
            }
        }
        items(itens, key = ItemDaFila::id) { item ->
            CartaoDaFila(
                item = item,
                progresso = progresso[item.id],
                aoAbrir = { item.videoIds.firstOrNull()?.let(navegacao::abrirVideo) },
                aoCancelar = { viewModel.cancelar(item.id) },
                aoRemover = { viewModel.remover(item.id) },
                atualizandoExtratores = item.id in atualizando,
                aoTentarDeNovo = { viewModel.tentarDeNovo(item.id) },
                aoAtualizarETentar = { viewModel.atualizarExtratoresETentar(item.id) },
            )
        }
    }
}

/** Cartão de destaque com o campo do Link, a Pasta de destino e o botão Baixar. */
@Composable
private fun CartaoDePedido(
    link: String,
    aoMudarLink: (String) -> Unit,
    pasta: String,
    aoTrocarPasta: () -> Unit,
    aviso: Aviso?,
    aoBaixar: () -> Unit,
) {
    val context = LocalContext.current
    val cores = MaterialTheme.colorScheme
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier
                .background(Brush.linearGradient(listOf(cores.primaryContainer, cores.tertiaryContainer)))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.baixar_titulo),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = cores.onPrimaryContainer,
            )
            Text(
                stringResource(R.string.baixar_subtitulo),
                style = MaterialTheme.typography.bodyMedium,
                color = cores.onPrimaryContainer.copy(alpha = 0.8f),
            )
            OutlinedTextField(
                value = link,
                onValueChange = aoMudarLink,
                placeholder = { Text(stringResource(R.string.baixar_link_rotulo)) },
                leadingIcon = { Icon(Icons.Filled.Link, contentDescription = null) },
                trailingIcon = {
                    if (link.isEmpty()) {
                        IconButton(onClick = { context.textoDaAreaDeTransferencia()?.let(aoMudarLink) }) {
                            Icon(Icons.Filled.ContentPaste, contentDescription = stringResource(R.string.baixar_colar))
                        }
                    } else {
                        IconButton(onClick = { aoMudarLink("") }) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cancelar))
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = cores.surface,
                    unfocusedContainerColor = cores.surface,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Plataforma.entries.forEach { SeloDePlataforma(it) }
            }
            AssistChip(
                onClick = aoTrocarPasta,
                label = { Text(stringResource(R.string.baixar_pasta, pasta), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                leadingIcon = { Icon(Icons.Outlined.Folder, contentDescription = null, Modifier.size(AssistChipDefaults.IconSize)) },
                trailingIcon = { Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.baixar_trocar_pasta), Modifier.size(AssistChipDefaults.IconSize)) },
                colors = AssistChipDefaults.assistChipColors(containerColor = cores.surface.copy(alpha = 0.6f)),
                border = null,
            )
            Button(
                onClick = aoBaixar,
                enabled = link.isNotBlank(),
                contentPadding = PaddingValues(vertical = 14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Download, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.baixar_botao), style = MaterialTheme.typography.titleSmall)
            }
            when (aviso) {
                Aviso.AdicionadoAFila -> Text(
                    stringResource(R.string.adicionado_a_fila),
                    color = cores.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Aviso.LinkRecusado -> Text(
                    stringResource(
                        R.string.link_nao_suportado,
                        stringResource(R.string.plataformas_suportadas, plataformasSuportadas()),
                    ),
                    color = cores.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
                null -> Unit
            }
        }
    }
}

private fun Context.textoDaAreaDeTransferencia(): String? =
    getSystemService(ClipboardManager::class.java)?.primaryClip?.getItemAt(0)?.coerceToText(this)?.toString()

@Composable
private fun CartaoDaFila(
    item: ItemDaFila,
    progresso: Float?,
    aoAbrir: () -> Unit,
    aoCancelar: () -> Unit,
    aoRemover: () -> Unit,
    atualizandoExtratores: Boolean,
    aoTentarDeNovo: () -> Unit,
    aoAtualizarETentar: () -> Unit,
) {
    val concluido = item.estado == EstadoNaFila.CONCLUIDO
    val falhou = item.estado == EstadoNaFila.FALHOU
    ElevatedCard(
        onClick = aoAbrir,
        enabled = concluido,
        colors = CardDefaults.elevatedCardColors(disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp), verticalAlignment = Alignment.Top) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(item.plataforma.cor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(iconeDoEstado(item.estado), contentDescription = null, tint = item.plataforma.cor)
            }
            Column(Modifier.weight(1f).padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SeloDePlataforma(item.plataforma)
                Text(
                    item.titulo ?: item.link,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    descricaoDoEstado(item),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (falhou) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (item.estado == EstadoNaFila.EM_ANDAMENTO) {
                    val modificador = Modifier.fillMaxWidth().padding(top = 4.dp).height(6.dp).clip(CircleShape)
                    if (progresso == null) {
                        LinearProgressIndicator(modificador)
                    } else {
                        LinearProgressIndicator(progress = { progresso }, modifier = modificador)
                    }
                }
                val falha = item.falha
                if (falhou && falha != null) {
                    when (AcaoAposFalha.para(falha)) {
                        AcaoAposFalha.ATUALIZAR_EXTRATORES_E_TENTAR -> FilledTonalButton(
                            onClick = aoAtualizarETentar,
                            enabled = !atualizandoExtratores,
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                stringResource(
                                    if (atualizandoExtratores) R.string.extratores_atualizando else R.string.falha_atualizar_e_tentar,
                                ),
                            )
                        }
                        AcaoAposFalha.TENTAR_DE_NOVO -> FilledTonalButton(onClick = aoTentarDeNovo) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.falha_tentar_de_novo))
                        }
                        AcaoAposFalha.NENHUMA -> Unit
                    }
                }
            }
            if (item.estado.encerrado) {
                IconButton(onClick = aoRemover) {
                    Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.fila_remover))
                }
            } else {
                IconButton(onClick = aoCancelar) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.fila_cancelar))
                }
            }
        }
    }
}

private fun iconeDoEstado(estado: EstadoNaFila): ImageVector = when (estado) {
    EstadoNaFila.PENDENTE -> Icons.Outlined.Schedule
    EstadoNaFila.AGUARDANDO_REDE -> Icons.Outlined.WifiOff
    EstadoNaFila.EM_ANDAMENTO -> Icons.Filled.Downloading
    EstadoNaFila.CONCLUIDO -> Icons.Filled.CheckCircle
    EstadoNaFila.FALHOU -> Icons.Filled.ErrorOutline
    EstadoNaFila.CANCELADO -> Icons.Outlined.Block
}

@Composable
private fun mensagemDeFalha(item: ItemDaFila): String = when (item.falha) {
    TipoDeFalha.PLATAFORMA_MUDOU -> stringResource(R.string.falha_plataforma_mudou, item.plataforma.nome)
    TipoDeFalha.EXIGE_LOGIN -> stringResource(R.string.falha_exige_login)
    TipoDeFalha.SEM_ESPACO -> stringResource(R.string.falha_sem_espaco)
    TipoDeFalha.SEM_REDE, TipoDeFalha.OUTRO, null -> stringResource(R.string.estado_falhou)
}

@Composable
fun descricaoDoEstado(item: ItemDaFila): String = when (item.estado) {
    EstadoNaFila.PENDENTE -> stringResource(R.string.estado_pendente)
    EstadoNaFila.AGUARDANDO_REDE -> stringResource(R.string.estado_aguardando_rede)
    EstadoNaFila.EM_ANDAMENTO -> stringResource(R.string.estado_em_andamento)
    EstadoNaFila.CONCLUIDO -> stringResource(R.string.estado_concluido)
    EstadoNaFila.FALHOU -> mensagemDeFalha(item)
    EstadoNaFila.CANCELADO -> stringResource(R.string.estado_cancelado)
}
