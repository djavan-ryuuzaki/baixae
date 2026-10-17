package br.com.djavanfernando.baixae.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.djavanfernando.baixae.Dependencias
import br.com.djavanfernando.baixae.R
import br.com.djavanfernando.baixae.core.Biblioteca
import br.com.djavanfernando.baixae.core.BibliotecaJaExiste
import br.com.djavanfernando.baixae.core.NomeInvalido
import kotlinx.coroutines.flow.combine

/** [capa] é o arquivo do Vídeo mais recente da Biblioteca, para a miniatura do cartão. */
data class ResumoDeBiblioteca(val biblioteca: Biblioteca, val quantidade: Int, val capa: String?)

/** Bibliotecas padrão na ordem das Plataformas, seguidas das personalizadas em ordem alfabética. */
fun ordenarBibliotecas(bibliotecas: List<Biblioteca>): List<Biblioteca> =
    bibliotecas.sortedWith(compareBy({ it.plataforma?.ordinal ?: Int.MAX_VALUE }, { it.nome.lowercase() }))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibliotecasTela(dependencias: Dependencias, aoAbrir: (String) -> Unit, modifier: Modifier = Modifier) {
    val repositorio = dependencias.repositorio
    val resumos by androidx.compose.runtime.remember {
        combine(repositorio.observarBibliotecas(), repositorio.observarVideos()) { bibliotecas, videos ->
            val porBiblioteca = videos.groupBy { it.bibliotecaId }
            ordenarBibliotecas(bibliotecas).map { biblioteca ->
                val daBiblioteca = porBiblioteca[biblioteca.id].orEmpty()
                val capa = daBiblioteca.filterNot { it.arquivoAusente }.maxByOrNull { it.dataDoDownload }?.caminhoDoArquivo
                ResumoDeBiblioteca(biblioteca, daBiblioteca.size, capa)
            }
        }
    }.collectAsStateWithLifecycle(emptyList())

    var criando by rememberSaveable { mutableStateOf(false) }

    Column(modifier.fillMaxSize()) {
        TituloDeTela(
            stringResource(R.string.aba_bibliotecas),
            acoes = {
                FilledTonalIconButton(onClick = { criando = true }, modifier = Modifier.padding(end = 8.dp)) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.biblioteca_nova))
                }
            },
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(resumos, key = { it.biblioteca.id }) { resumo ->
                CartaoDeBiblioteca(resumo, aoAbrir = { aoAbrir(resumo.biblioteca.id) })
            }
        }
    }
    if (criando) {
        val jaExiste = stringResource(R.string.erro_biblioteca_ja_existe)
        val invalido = stringResource(R.string.erro_nome_invalido)
        DialogoDeTexto(
            titulo = stringResource(R.string.biblioteca_nova),
            valorInicial = "",
            rotulo = stringResource(R.string.video_nome),
            aoConfirmar = { nome ->
                try {
                    aoAbrir(dependencias.acervo.criarBiblioteca(nome).id)
                    null
                } catch (_: BibliotecaJaExiste) {
                    jaExiste
                } catch (_: NomeInvalido) {
                    invalido
                }
            },
            aoDispensar = { criando = false },
        )
    }
}

@Composable
private fun CartaoDeBiblioteca(resumo: ResumoDeBiblioteca, aoAbrir: () -> Unit) {
    val cor = resumo.biblioteca.plataforma?.cor ?: MaterialTheme.colorScheme.primary
    ElevatedCard(onClick = aoAbrir, modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().aspectRatio(4f / 3f)) {
            val capa = resumo.capa
            if (capa != null) {
                Miniatura(capa, Modifier.matchParentSize())
            } else {
                Box(
                    Modifier.matchParentSize().background(Brush.linearGradient(listOf(cor.copy(alpha = 0.35f), cor.copy(alpha = 0.1f)))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (resumo.biblioteca.padrao) Icons.Outlined.VideoLibrary else Icons.Outlined.Folder,
                        contentDescription = null,
                        tint = cor,
                        modifier = Modifier.size(40.dp),
                    )
                }
            }
            Box(Modifier.fillMaxWidth().height(4.dp).align(Alignment.BottomCenter).background(cor))
        }
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                nomeDaBiblioteca(resumo.biblioteca),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                pluralStringResource(R.plurals.quantidade_de_videos, resumo.quantidade, resumo.quantidade),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
