package br.com.djavanfernando.baixae.ui

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import br.com.djavanfernando.baixae.R
import br.com.djavanfernando.baixae.core.Video
import java.io.File

/** Reproduz o Vídeo no player do sistema. */
fun Context.reproduzir(video: Video) = abrir(
    Intent(Intent.ACTION_VIEW)
        .setDataAndType(Uri.parse(video.caminhoDoArquivo), "video/mp4")
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
)

/** Abre o Link do Post de onde o Vídeo veio. */
fun Context.abrirOriginal(video: Video) = abrir(Intent(Intent.ACTION_VIEW, Uri.parse(video.link)))

/** Abre o menu Compartilhar do Android com o arquivo (URI `content://` ou caminho de uma cópia em cache). */
fun Context.compartilhar(caminho: String) {
    val uri = if (caminho.startsWith("content://")) {
        Uri.parse(caminho)
    } else {
        FileProvider.getUriForFile(this, "$packageName.arquivos", File(caminho))
    }
    val envio = Intent(Intent.ACTION_SEND)
        .setType("video/mp4")
        .putExtra(Intent.EXTRA_STREAM, uri)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    // Sem ClipData, a permissão de leitura não chega ao app escolhido no menu do sistema.
    envio.clipData = ClipData.newRawUri(null, uri)
    abrir(Intent.createChooser(envio, getString(R.string.video_compartilhar)))
}

private fun Context.abrir(intent: Intent) {
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(this, R.string.nenhum_app_para_abrir, Toast.LENGTH_SHORT).show()
    }
}
