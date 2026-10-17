package br.com.djavanfernando.baixae.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import br.com.djavanfernando.baixae.Dependencias
import br.com.djavanfernando.baixae.R

enum class Aba(val rotulo: Int, val icone: ImageVector) {
    BAIXAR(R.string.aba_baixar, Icons.Filled.Download),
    BIBLIOTECAS(R.string.aba_bibliotecas, Icons.Filled.VideoLibrary),
    CONFIGURACOES(R.string.aba_configuracoes, Icons.Filled.Settings),
}

/** Navegação do app: três abas e, por cima delas, a Biblioteca e o Vídeo abertos. */
class Navegacao(
    aba: Aba = Aba.BAIXAR,
    bibliotecaAberta: String? = null,
    videoAberto: String? = null,
) {
    var aba by mutableStateOf(aba)
    var bibliotecaAberta by mutableStateOf(bibliotecaAberta)
    var videoAberto by mutableStateOf(videoAberto)

    fun abrirVideo(id: String) {
        videoAberto = id
    }

    fun voltar(): Boolean = when {
        videoAberto != null -> true.also { videoAberto = null }
        aba == Aba.BIBLIOTECAS && bibliotecaAberta != null -> true.also { bibliotecaAberta = null }
        aba != Aba.BAIXAR -> true.also { aba = Aba.BAIXAR }
        else -> false
    }
}

@Composable
fun App(dependencias: Dependencias, navegacao: Navegacao) {
    BackHandler(enabled = navegacao.videoAberto != null || navegacao.aba != Aba.BAIXAR || navegacao.bibliotecaAberta != null) {
        navegacao.voltar()
    }
    val videoAberto = navegacao.videoAberto
    if (videoAberto != null) {
        VideoTela(dependencias, videoAberto, aoVoltar = { navegacao.videoAberto = null })
        return
    }
    Scaffold(
        bottomBar = {
            NavigationBar {
                Aba.entries.forEach { aba ->
                    NavigationBarItem(
                        selected = navegacao.aba == aba,
                        onClick = {
                            if (navegacao.aba == aba) navegacao.bibliotecaAberta = null
                            navegacao.aba = aba
                        },
                        icon = { Icon(aba.icone, contentDescription = null) },
                        label = { Text(stringResource(aba.rotulo)) },
                    )
                }
            }
        },
    ) { margens ->
        val modifier = Modifier.padding(margens)
        when (navegacao.aba) {
            Aba.BAIXAR -> BaixarTela(dependencias, navegacao, modifier)
            Aba.BIBLIOTECAS -> {
                val aberta = navegacao.bibliotecaAberta
                if (aberta == null) {
                    BibliotecasTela(dependencias, aoAbrir = { navegacao.bibliotecaAberta = it }, modifier)
                } else {
                    BibliotecaTela(
                        dependencias,
                        aberta,
                        aoAbrirVideo = navegacao::abrirVideo,
                        aoVoltar = { navegacao.bibliotecaAberta = null },
                        modifier,
                    )
                }
            }
            Aba.CONFIGURACOES -> ConfiguracoesTela(dependencias, modifier)
        }
    }
}

private val SalvadorDeNavegacao = listSaver<Navegacao, String?>(
    save = { listOf(it.aba.name, it.bibliotecaAberta, it.videoAberto) },
    restore = { Navegacao(Aba.valueOf(it[0]!!), it[1], it[2]) },
)

@Composable
fun rememberNavegacao(): Navegacao = rememberSaveable(saver = SalvadorDeNavegacao) { Navegacao() }
