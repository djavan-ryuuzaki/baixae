package br.com.djavanfernando.baixae.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import br.com.djavanfernando.baixae.R
import br.com.djavanfernando.baixae.core.Plataforma

/** Nome da Plataforma para exibição, com a marca de Plataforma instável quando for o caso. */
fun rotulo(context: Context, plataforma: Plataforma): String =
    if (plataforma.instavel) context.getString(R.string.plataforma_instavel, plataforma.nome) else plataforma.nome

fun plataformasSuportadas(context: Context): String = Plataforma.entries.joinToString(", ") { rotulo(context, it) }

@Composable
fun rotulo(plataforma: Plataforma): String = rotulo(LocalContext.current, plataforma)

@Composable
fun plataformasSuportadas(): String = plataformasSuportadas(LocalContext.current)
