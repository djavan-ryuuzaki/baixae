package br.com.djavanfernando.socialvideodownloader

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import br.com.djavanfernando.socialvideodownloader.ui.BaixarTela
import br.com.djavanfernando.socialvideodownloader.ui.Tema

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
                .launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        val dependencias = (application as SocialVideoDownloaderApp).dependencias
        setContent {
            Tema { BaixarTela(dependencias) }
        }
    }
}
