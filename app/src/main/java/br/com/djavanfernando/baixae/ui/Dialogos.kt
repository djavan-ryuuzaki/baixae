package br.com.djavanfernando.baixae.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import br.com.djavanfernando.baixae.R

/**
 * Diálogo com um campo de texto. [aoConfirmar] devolve uma mensagem de erro para manter o diálogo aberto,
 * ou `null` para fechá-lo.
 */
@Composable
fun DialogoDeTexto(
    titulo: String,
    valorInicial: String,
    rotulo: String,
    aoConfirmar: suspend (String) -> String?,
    aoDispensar: () -> Unit,
    ajuda: String? = null,
) {
    var valor by remember { mutableStateOf(valorInicial) }
    var erro by remember { mutableStateOf<String?>(null) }
    var confirmar by remember { mutableStateOf(false) }
    if (confirmar) {
        androidx.compose.runtime.LaunchedEffect(Unit) {
            erro = aoConfirmar(valor)
            confirmar = false
            if (erro == null) aoDispensar()
        }
    }
    AlertDialog(
        onDismissRequest = aoDispensar,
        title = { Text(titulo) },
        text = {
            OutlinedTextField(
                value = valor,
                onValueChange = {
                    valor = it
                    erro = null
                },
                label = { Text(rotulo) },
                isError = erro != null,
                supportingText = (erro ?: ajuda)?.let { { Text(it) } },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { confirmar = true }, enabled = !confirmar) { Text(stringResource(R.string.confirmar)) }
        },
        dismissButton = { TextButton(onClick = aoDispensar) { Text(stringResource(R.string.cancelar)) } },
    )
}
