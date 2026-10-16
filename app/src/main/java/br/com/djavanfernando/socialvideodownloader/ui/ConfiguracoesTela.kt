package br.com.djavanfernando.socialvideodownloader.ui

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.djavanfernando.socialvideodownloader.Dependencias
import br.com.djavanfernando.socialvideodownloader.R
import br.com.djavanfernando.socialvideodownloader.core.Configuracoes
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

    Column(modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.aba_configuracoes)) })
        Column(Modifier.verticalScroll(rememberScrollState())) {
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
}
