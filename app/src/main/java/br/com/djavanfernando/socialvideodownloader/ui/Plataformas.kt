package br.com.djavanfernando.socialvideodownloader.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import br.com.djavanfernando.socialvideodownloader.R
import br.com.djavanfernando.socialvideodownloader.core.Plataforma

/** Nome da Plataforma para exibição, com a marca de Plataforma instável quando for o caso. */
@Composable
fun rotulo(plataforma: Plataforma): String =
    if (plataforma.instavel) stringResource(R.string.plataforma_instavel, plataforma.nome) else plataforma.nome

@Composable
fun plataformasSuportadas(): String = Plataforma.entries.map { rotulo(it) }.joinToString(", ")
