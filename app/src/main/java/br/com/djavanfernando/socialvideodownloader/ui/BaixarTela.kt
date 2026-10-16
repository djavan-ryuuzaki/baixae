package br.com.djavanfernando.socialvideodownloader.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.djavanfernando.socialvideodownloader.Dependencias
import br.com.djavanfernando.socialvideodownloader.R
import br.com.djavanfernando.socialvideodownloader.core.LinkNaoSuportado
import br.com.djavanfernando.socialvideodownloader.core.Video
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface EstadoDoDownload {
    data object Parado : EstadoDoDownload
    data object Concluido : EstadoDoDownload
    data class Falhou(val motivo: String) : EstadoDoDownload
    data object LinkRecusado : EstadoDoDownload
}

class BaixarViewModel(private val dependencias: Dependencias) : ViewModel() {
    val link = MutableStateFlow("")
    val estado = MutableStateFlow<EstadoDoDownload>(EstadoDoDownload.Parado)
    val baixando = MutableStateFlow(false)
    val videos = dependencias.repositorio.observarVideos()
        .map { lista -> lista.sortedByDescending(Video::dataDoDownload) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun baixar() {
        val alvo = link.value.trim()
        if (alvo.isEmpty() || baixando.value) return
        baixando.value = true
        estado.value = EstadoDoDownload.Parado
        viewModelScope.launch {
            estado.value = try {
                dependencias.baixador.baixar(alvo, PASTA_PADRAO)
                link.value = ""
                EstadoDoDownload.Concluido
            } catch (_: LinkNaoSuportado) {
                EstadoDoDownload.LinkRecusado
            } catch (erro: Exception) {
                EstadoDoDownload.Falhou(erro.message ?: erro.javaClass.simpleName)
            } finally {
                baixando.value = false
            }
        }
    }

    private companion object {
        const val PASTA_PADRAO = "Movies/SocialVideoDownloader"
    }
}

@Composable
fun BaixarTela(dependencias: Dependencias) {
    val viewModel = viewModel { BaixarViewModel(dependencias) }
    val link by viewModel.link.collectAsStateWithLifecycle()
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val baixando by viewModel.baixando.collectAsStateWithLifecycle()
    val videos by viewModel.videos.collectAsStateWithLifecycle()

    Scaffold { margens ->
        Column(
            Modifier.fillMaxSize().padding(margens).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = link,
                onValueChange = { viewModel.link.value = it },
                label = { Text(stringResource(R.string.baixar_link_rotulo)) },
                supportingText = { Text(stringResource(R.string.plataformas_suportadas, plataformasSuportadas())) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(onClick = viewModel::baixar, enabled = !baixando, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.baixar_botao))
            }
            when (val atual = estado) {
                EstadoDoDownload.Parado -> if (baixando) Text(stringResource(R.string.baixar_baixando))
                EstadoDoDownload.Concluido -> Text(stringResource(R.string.baixar_concluido))
                is EstadoDoDownload.Falhou -> Text(
                    stringResource(R.string.baixar_falhou, atual.motivo),
                    color = MaterialTheme.colorScheme.error,
                )
                EstadoDoDownload.LinkRecusado -> Text(
                    stringResource(
                        R.string.link_nao_suportado,
                        stringResource(R.string.plataformas_suportadas, plataformasSuportadas()),
                    ),
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Text(stringResource(R.string.videos_titulo), style = MaterialTheme.typography.titleMedium)
            if (videos.isEmpty()) Text(stringResource(R.string.videos_vazio))
            LazyColumn {
                items(videos, key = Video::id) { video -> ListItem(headlineContent = { Text(video.nome) }) }
            }
        }
    }
}
