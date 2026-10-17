package br.com.djavanfernando.baixae

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import br.com.djavanfernando.baixae.core.LinkNaoSuportado
import br.com.djavanfernando.baixae.core.LinkRepetido
import br.com.djavanfernando.baixae.core.Plataforma
import br.com.djavanfernando.baixae.fila.AgendadorDaFila
import br.com.djavanfernando.baixae.ui.plataformasSuportadas
import br.com.djavanfernando.baixae.ui.rotulo
import kotlinx.coroutines.launch

/**
 * Destino do menu Compartilhar: enfileira o Link recebido na Pasta de destino padrão e fecha,
 * deixando o usuário no app de origem. Só mostra algo além de um aviso quando o Link já foi baixado.
 */
class CompartilharActivity : ComponentActivity() {
    private val dependencias get() = (application as BaixaeApp).dependencias

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) return
        val texto = intent?.takeIf { it.action == Intent.ACTION_SEND }?.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
        val link = Plataforma.extrairLink(texto)
        if (link == null || Plataforma.reconhecer(link) == null) {
            avisar(getString(R.string.compartilhar_recusado, plataformasSuportadas(this)))
            finish()
            return
        }
        lifecycleScope.launch {
            val anterior = dependencias.acervo.downloadAnterior(link)
            if (anterior == null) {
                enfileirar(link)
                finish()
            } else {
                perguntarSobreRepetido(link, anterior)
            }
        }
    }

    private fun perguntarSobreRepetido(link: String, anterior: LinkRepetido) {
        val biblioteca = anterior.biblioteca.plataforma?.let { rotulo(this, it) } ?: anterior.biblioteca.nome
        AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
            .setTitle(R.string.repetido_titulo)
            .setMessage(getString(R.string.repetido_texto, anterior.video.nome, biblioteca))
            .setPositiveButton(R.string.repetido_abrir) { _, _ ->
                startActivity(MainActivity.abrirVideo(this, anterior.video.id))
            }
            .setNegativeButton(R.string.repetido_baixar_de_novo) { _, _ ->
                dependencias.escopo.launch { enfileirar(link) }
            }
            .setOnDismissListener { finish() }
            .show()
    }

    private suspend fun enfileirar(link: String) {
        try {
            dependencias.fila.enfileirar(link)
            AgendadorDaFila.iniciar(applicationContext)
            avisar(getString(R.string.adicionado_a_fila))
        } catch (_: LinkNaoSuportado) {
            avisar(getString(R.string.compartilhar_recusado, plataformasSuportadas(this)))
        }
    }

    private fun avisar(texto: String) {
        val contexto = applicationContext
        runOnUiThread { Toast.makeText(contexto, texto, Toast.LENGTH_LONG).show() }
    }
}
