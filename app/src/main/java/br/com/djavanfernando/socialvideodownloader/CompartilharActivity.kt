package br.com.djavanfernando.socialvideodownloader

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import br.com.djavanfernando.socialvideodownloader.core.LinkNaoSuportado
import br.com.djavanfernando.socialvideodownloader.core.Plataforma
import br.com.djavanfernando.socialvideodownloader.fila.AgendadorDaFila
import br.com.djavanfernando.socialvideodownloader.ui.plataformasSuportadas
import kotlinx.coroutines.launch

/**
 * Destino do menu Compartilhar: enfileira o Link recebido na Pasta de destino padrão e fecha,
 * deixando o usuário no app de origem.
 */
class CompartilharActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val texto = intent?.takeIf { it.action == Intent.ACTION_SEND }?.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
        val link = Plataforma.extrairLink(texto)
        val dependencias = (application as SocialVideoDownloaderApp).dependencias
        if (link == null) {
            recusar()
        } else {
            dependencias.escopo.launch {
                try {
                    dependencias.fila.enfileirar(link)
                    AgendadorDaFila.iniciar(applicationContext)
                    avisar(getString(R.string.adicionado_a_fila))
                } catch (_: LinkNaoSuportado) {
                    recusar()
                }
            }
        }
        finish()
    }

    private fun recusar() = avisar(getString(R.string.compartilhar_recusado, plataformasSuportadas(this)))

    private fun avisar(texto: String) {
        val contexto = applicationContext
        Handler(Looper.getMainLooper()).post { Toast.makeText(contexto, texto, Toast.LENGTH_LONG).show() }
    }
}
