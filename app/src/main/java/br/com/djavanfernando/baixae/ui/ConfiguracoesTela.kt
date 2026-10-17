package br.com.djavanfernando.baixae.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrandingWatermark
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.content.Intent
import android.net.Uri
import br.com.djavanfernando.baixae.BuildConfig
import br.com.djavanfernando.baixae.Dependencias
import br.com.djavanfernando.baixae.R
import br.com.djavanfernando.baixae.core.Configuracoes
import br.com.djavanfernando.baixae.core.LimiteDeQualidade
import br.com.djavanfernando.baixae.core.PastaDeDestino
import br.com.djavanfernando.baixae.core.Versao
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracoesTela(dependencias: Dependencias, modifier: Modifier = Modifier) {
    val preferencias = dependencias.preferencias
    val configuracoes by preferencias.observar().collectAsStateWithLifecycle(Configuracoes())
    val escopo = rememberCoroutineScope()
    fun alterar(mudanca: (Configuracoes) -> Configuracoes) {
        escopo.launch { preferencias.salvar(mudanca(preferencias.ler())) }
    }

    var editandoPasta by rememberSaveable { mutableStateOf(false) }
    var escolhendoQualidade by rememberSaveable { mutableStateOf(false) }
    var verificandoVersao by remember { mutableStateOf(false) }
    var resultadoDaVerificacao by remember { mutableStateOf<String?>(null) }
    val versaoMaisRecente = stringResource(R.string.versao_mais_recente)
    val novaVersao = configuracoes.novaVersao
        ?.takeIf { Versao.maisNova(it.versao, BuildConfig.VERSION_NAME) }
    val context = LocalContext.current
    var versaoDosExtratores by remember { mutableStateOf<String?>(null) }
    var atualizandoExtratores by remember { mutableStateOf(false) }
    var resultadoDaAtualizacao by remember { mutableStateOf<String?>(null) }
    val falhaAoAtualizar = stringResource(R.string.extratores_falha_ao_atualizar)
    val atualizados = stringResource(R.string.extratores_atualizados)
    LaunchedEffect(Unit) {
        versaoDosExtratores = try {
            dependencias.extratores.versao()
        } catch (_: Exception) {
            null
        }
    }

    Column(modifier.fillMaxSize()) {
        TituloDeTela(stringResource(R.string.aba_configuracoes))
        Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 16.dp)) {
            if (novaVersao != null) {
                Card(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.nova_versao_titulo, novaVersao.versao), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.nova_versao_texto))
                        TextButton(onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(novaVersao.endereco)))
                        }) { Text(stringResource(R.string.nova_versao_abrir)) }
                    }
                }
            }
            Secao(stringResource(R.string.config_secao_downloads)) {
                ItemDeConfiguracao(
                    icone = Icons.Outlined.Folder,
                    titulo = stringResource(R.string.config_pasta_padrao),
                    descricao = configuracoes.pastaPadrao,
                    modifier = Modifier.clickable { editandoPasta = true },
                )
                ItemDeConfiguracao(
                    icone = Icons.Outlined.HighQuality,
                    titulo = stringResource(R.string.config_qualidade),
                    descricao = rotuloDaQualidade(configuracoes.limiteDeQualidade),
                    modifier = Modifier.clickable { escolhendoQualidade = true },
                )
                ItemDeConfiguracao(
                    icone = Icons.Outlined.Wifi,
                    titulo = stringResource(R.string.config_so_wifi),
                    descricao = stringResource(R.string.config_so_wifi_descricao),
                    modifier = Modifier.toggleable(configuracoes.soWifi) { ligado -> alterar { it.copy(soWifi = ligado) } },
                ) { Switch(checked = configuracoes.soWifi, onCheckedChange = null) }
            }
            Secao(stringResource(R.string.config_secao_compartilhar)) {
                ItemDeConfiguracao(
                    icone = Icons.Outlined.BrandingWatermark,
                    titulo = stringResource(R.string.config_marca_dagua),
                    descricao = stringResource(R.string.config_marca_dagua_descricao),
                    modifier = Modifier.toggleable(configuracoes.marcaDagua) { ligado -> alterar { it.copy(marcaDagua = ligado) } },
                ) { Switch(checked = configuracoes.marcaDagua, onCheckedChange = null) }
            }
            Secao(stringResource(R.string.config_secao_sobre)) {
                ItemDeConfiguracao(
                    icone = Icons.Outlined.Extension,
                    titulo = stringResource(R.string.extratores_titulo),
                    descricao = resultadoDaAtualizacao ?: stringResource(R.string.extratores_versao, versaoDosExtratores ?: "…"),
                ) {
                    FilledTonalButton(
                        enabled = !atualizandoExtratores,
                        onClick = {
                            atualizandoExtratores = true
                            resultadoDaAtualizacao = null
                            escopo.launch {
                                resultadoDaAtualizacao = try {
                                    versaoDosExtratores = dependencias.extratores.atualizar()
                                    atualizados
                                } catch (erro: CancellationException) {
                                    throw erro
                                } catch (_: Exception) {
                                    falhaAoAtualizar
                                } finally {
                                    atualizandoExtratores = false
                                }
                            }
                        },
                    ) {
                        Text(
                            stringResource(
                                if (atualizandoExtratores) R.string.extratores_atualizando else R.string.extratores_atualizar,
                            ),
                        )
                    }
                }
                ItemDeConfiguracao(
                    icone = Icons.Outlined.Info,
                    titulo = stringResource(R.string.versao_do_app, BuildConfig.VERSION_NAME),
                    descricao = resultadoDaVerificacao,
                )
                TextButton(
                    enabled = !verificandoVersao,
                    onClick = {
                        verificandoVersao = true
                        resultadoDaVerificacao = null
                        escopo.launch {
                            val encontrada = dependencias.atualizacoesDoApp.verificar()
                            resultadoDaVerificacao = if (encontrada == null) versaoMaisRecente else null
                            verificandoVersao = false
                        }
                    },
                    modifier = Modifier.padding(start = 64.dp, bottom = 8.dp),
                ) { Text(stringResource(R.string.verificar_atualizacoes)) }
            }
        }
    }
    if (escolhendoQualidade) {
        AlertDialog(
            onDismissRequest = { escolhendoQualidade = false },
            title = { Text(stringResource(R.string.config_qualidade)) },
            text = {
                Column {
                    LimiteDeQualidade.entries.forEach { limite ->
                        Row(
                            Modifier.fillMaxWidth().selectable(configuracoes.limiteDeQualidade == limite) {
                                alterar { it.copy(limiteDeQualidade = limite) }
                                escolhendoQualidade = false
                            },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = configuracoes.limiteDeQualidade == limite, onClick = null)
                            Text(rotuloDaQualidade(limite))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { escolhendoQualidade = false }) { Text(stringResource(R.string.cancelar)) } },
        )
    }
    if (editandoPasta) {
        DialogoDePasta(
            titulo = stringResource(R.string.config_pasta_padrao),
            pastaAtual = configuracoes.pastaPadrao,
            aoEscolher = { pasta -> alterar { it.copy(pastaPadrao = pasta) } },
            aoDispensar = { editandoPasta = false },
        )
    }
}

@Composable
private fun rotuloDaQualidade(limite: LimiteDeQualidade): String =
    limite.resolucaoMaxima?.let { stringResource(R.string.qualidade_ate, it) } ?: stringResource(R.string.qualidade_maxima)

/** Diálogo para escolher uma Pasta de destino, validada pelas regras do núcleo. */
@Composable
fun DialogoDePasta(titulo: String, pastaAtual: String, aoEscolher: (String) -> Unit, aoDispensar: () -> Unit) {
    val invalida = stringResource(R.string.erro_pasta_invalida, PastaDeDestino.raizesPermitidas.joinToString(", "))
    DialogoDeTexto(
        titulo = titulo,
        valorInicial = pastaAtual,
        rotulo = stringResource(R.string.pasta_rotulo),
        ajuda = stringResource(R.string.pasta_ajuda, PastaDeDestino.raizesPermitidas.joinToString(", ")),
        aoConfirmar = { texto ->
            val pasta = PastaDeDestino.validar(texto)
            if (pasta == null) invalida else null.also { aoEscolher(pasta) }
        },
        aoDispensar = aoDispensar,
    )
}

@Composable
private fun ItemDeConfiguracao(
    icone: ImageVector,
    titulo: String,
    descricao: String?,
    modifier: Modifier = Modifier,
    final: (@Composable () -> Unit)? = null,
) {
    ListItem(
        modifier = modifier,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = { IconeRedondo(icone) },
        headlineContent = { Text(titulo) },
        supportingContent = descricao?.let { { Text(it) } },
        trailingContent = final,
    )
}
