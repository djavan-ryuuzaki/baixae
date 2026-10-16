package br.com.djavanfernando.socialvideodownloader

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import br.com.djavanfernando.socialvideodownloader.ui.App
import br.com.djavanfernando.socialvideodownloader.ui.Tema
import br.com.djavanfernando.socialvideodownloader.ui.rememberNavegacao

class MainActivity : ComponentActivity() {
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
        val dependencias = (application as SocialVideoDownloaderApp).dependencias
        setContent {
            Tema { App(dependencias, rememberNavegacao()) }
        }
    }
}
