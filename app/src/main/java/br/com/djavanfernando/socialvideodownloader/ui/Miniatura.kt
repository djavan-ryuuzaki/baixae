package br.com.djavanfernando.socialvideodownloader.ui

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val cacheDeMiniaturas = LruCache<String, Bitmap>(64)

/** Miniatura de um Vídeo gerada a partir do próprio arquivo, para funcionar sem rede. */
@Composable
fun Miniatura(caminhoDoArquivo: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val imagem by produceState(cacheDeMiniaturas.get(caminhoDoArquivo), caminhoDoArquivo) {
        if (value == null) value = withContext(Dispatchers.IO) { gerarMiniatura(context, caminhoDoArquivo) }
    }
    Box(modifier.background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
        val atual = imagem
        if (atual != null) {
            Image(atual.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize())
        } else {
            Icon(Icons.Filled.Movie, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun gerarMiniatura(context: Context, caminho: String): Bitmap? = try {
    MediaMetadataRetriever().run {
        try {
            setDataSource(context, Uri.parse(caminho))
            getFrameAtTime(1_000_000)?.let { quadro ->
                val largura = 320
                val altura = (quadro.height * largura / quadro.width.coerceAtLeast(1)).coerceAtLeast(1)
                Bitmap.createScaledBitmap(quadro, largura, altura, true)
            }
        } finally {
            release()
        }
    }?.also { cacheDeMiniaturas.put(caminho, it) }
} catch (_: Exception) {
    null
}
