package br.com.djavanfernando.baixae

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import br.com.djavanfernando.baixae.ui.App
import br.com.djavanfernando.baixae.ui.Tema
import br.com.djavanfernando.baixae.ui.rememberNavegacao
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    /** Vídeo pedido por um Intent (ex.: "Abrir" num Link repetido vindo do menu Compartilhar). */
    private val videoPedido = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val permissoes = buildList {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (permissoes.isNotEmpty() && savedInstanceState == null) {
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}
                .launch(permissoes.toTypedArray())
        }
        if (savedInstanceState == null) receber(intent)
        val dependencias = (application as BaixaeApp).dependencias
        setContent {
            val navegacao = rememberNavegacao()
            LaunchedEffect(Unit) {
                videoPedido.collect { id ->
                    if (id != null) {
                        navegacao.abrirVideo(id)
                        videoPedido.value = null
                    }
                }
            }
            Tema { App(dependencias, navegacao) }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        receber(intent)
    }

    private fun receber(intent: Intent?) {
        intent?.getStringExtra(EXTRA_VIDEO)?.let { videoPedido.value = it }
    }

    companion object {
        private const val EXTRA_VIDEO = "video"

        fun abrirVideo(context: Context, videoId: String): Intent =
            Intent(context, MainActivity::class.java)
                .putExtra(EXTRA_VIDEO, videoId)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
}
