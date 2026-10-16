package br.com.djavanfernando.socialvideodownloader

import android.app.Application
import br.com.djavanfernando.socialvideodownloader.adaptadores.ArmazenamentoMediaStore
import br.com.djavanfernando.socialvideodownloader.adaptadores.ExtratorYoutubeDl
import br.com.djavanfernando.socialvideodownloader.core.Acervo
import br.com.djavanfernando.socialvideodownloader.core.Baixador
import br.com.djavanfernando.socialvideodownloader.core.portas.Relogio
import br.com.djavanfernando.socialvideodownloader.dados.BancoDeDados
import br.com.djavanfernando.socialvideodownloader.dados.RepositorioRoom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.Instant

class SocialVideoDownloaderApp : Application() {
    lateinit var dependencias: Dependencias
        private set

    override fun onCreate() {
        super.onCreate()
        dependencias = Dependencias(this)
        dependencias.escopo.launch { dependencias.acervo.iniciar() }
    }
}

/** Liga o núcleo aos adaptadores reais. */
class Dependencias(app: Application) {
    /** Escopo de trabalho que vive enquanto o processo do app viver. */
    val escopo = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val relogio = object : Relogio {
        override fun agora(): Instant = Instant.now()
    }
    val repositorio = RepositorioRoom(BancoDeDados.criar(app))
    val armazenamento = ArmazenamentoMediaStore(app)
    val extrator = ExtratorYoutubeDl(app)
    val acervo = Acervo(repositorio, armazenamento)
    val baixador = Baixador(extrator, armazenamento, repositorio, relogio)
}
