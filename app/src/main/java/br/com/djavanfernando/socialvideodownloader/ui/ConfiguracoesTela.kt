package br.com.djavanfernando.socialvideodownloader.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import br.com.djavanfernando.socialvideodownloader.Dependencias
import br.com.djavanfernando.socialvideodownloader.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracoesTela(@Suppress("UNUSED_PARAMETER") dependencias: Dependencias, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.aba_configuracoes)) })
    }
}
