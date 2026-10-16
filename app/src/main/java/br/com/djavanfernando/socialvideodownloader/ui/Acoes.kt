package br.com.djavanfernando.socialvideodownloader.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import br.com.djavanfernando.socialvideodownloader.R
import br.com.djavanfernando.socialvideodownloader.core.Video

/** Reproduz o Vídeo no player do sistema. */
fun Context.reproduzir(video: Video) = abrir(
    Intent(Intent.ACTION_VIEW)
        .setDataAndType(Uri.parse(video.caminhoDoArquivo), "video/mp4")
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
)

/** Abre o Link do Post de onde o Vídeo veio. */
fun Context.abrirOriginal(video: Video) = abrir(Intent(Intent.ACTION_VIEW, Uri.parse(video.link)))

private fun Context.abrir(intent: Intent) {
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(this, R.string.nenhum_app_para_abrir, Toast.LENGTH_SHORT).show()
    }
}
