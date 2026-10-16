package br.com.djavanfernando.socialvideodownloader.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.djavanfernando.socialvideodownloader.Dependencias
import br.com.djavanfernando.socialvideodownloader.R
import br.com.djavanfernando.socialvideodownloader.core.Configuracoes
import br.com.djavanfernando.socialvideodownloader.core.PastaDeDestino
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

    Column(modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.aba_configuracoes)) })
        Column(Modifier.verticalScroll(rememberScrollState())) {
            ListItem(
                modifier = Modifier.clickable { editandoPasta = true },
                headlineContent = { Text(stringResource(R.string.config_pasta_padrao)) },
                supportingContent = { Text(configuracoes.pastaPadrao) },
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.config_so_wifi)) },
                supportingContent = { Text(stringResource(R.string.config_so_wifi_descricao)) },
                trailingContent = {
                    Switch(
                        checked = configuracoes.soWifi,
                        onCheckedChange = { ligado -> alterar { it.copy(soWifi = ligado) } },
                    )
                },
            )
        }
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
