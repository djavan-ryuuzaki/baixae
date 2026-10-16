package br.com.djavanfernando.socialvideodownloader.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.djavanfernando.socialvideodownloader.Dependencias
import br.com.djavanfernando.socialvideodownloader.R
import br.com.djavanfernando.socialvideodownloader.core.Biblioteca
import br.com.djavanfernando.socialvideodownloader.core.BibliotecaJaExiste
import br.com.djavanfernando.socialvideodownloader.core.NomeInvalido
import kotlinx.coroutines.flow.combine

data class ResumoDeBiblioteca(val biblioteca: Biblioteca, val quantidade: Int)

/** Bibliotecas padrão na ordem das Plataformas, seguidas das personalizadas em ordem alfabética. */
fun ordenarBibliotecas(bibliotecas: List<Biblioteca>): List<Biblioteca> =
    bibliotecas.sortedWith(compareBy({ it.plataforma?.ordinal ?: Int.MAX_VALUE }, { it.nome.lowercase() }))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibliotecasTela(dependencias: Dependencias, aoAbrir: (String) -> Unit, modifier: Modifier = Modifier) {
    val repositorio = dependencias.repositorio
    val resumos by androidx.compose.runtime.remember {
        combine(repositorio.observarBibliotecas(), repositorio.observarVideos()) { bibliotecas, videos ->
            val porBiblioteca = videos.groupingBy { it.bibliotecaId }.eachCount()
            ordenarBibliotecas(bibliotecas).map { ResumoDeBiblioteca(it, porBiblioteca[it.id] ?: 0) }
        }
    }.collectAsStateWithLifecycle(emptyList())

    var criando by rememberSaveable { mutableStateOf(false) }

    Column(modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.aba_bibliotecas)) },
            actions = {
                IconButton(onClick = { criando = true }) {
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
                Card(Modifier.fillMaxWidth().clickable { aoAbrir(resumo.biblioteca.id) }) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(nomeDaBiblioteca(resumo.biblioteca), style = MaterialTheme.typography.titleMedium)
                        Text(
                            pluralStringResource(R.plurals.quantidade_de_videos, resumo.quantidade, resumo.quantidade),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
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
