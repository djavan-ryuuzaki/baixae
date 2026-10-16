package br.com.djavanfernando.socialvideodownloader

import android.app.Application
import br.com.djavanfernando.socialvideodownloader.adaptadores.ArmazenamentoMediaStore
import br.com.djavanfernando.socialvideodownloader.adaptadores.ExtratorYoutubeDl
import br.com.djavanfernando.socialvideodownloader.core.Baixador
import br.com.djavanfernando.socialvideodownloader.core.portas.Relogio
import br.com.djavanfernando.socialvideodownloader.dados.BancoDeDados
import br.com.djavanfernando.socialvideodownloader.dados.RepositorioRoom
import java.time.Instant

class SocialVideoDownloaderApp : Application() {
    lateinit var dependencias: Dependencias
        private set

    override fun onCreate() {
        super.onCreate()
        dependencias = Dependencias(this)
    }
}

/** Liga o núcleo aos adaptadores reais. */
class Dependencias(app: Application) {
    val relogio = object : Relogio {
        override fun agora(): Instant = Instant.now()
    }
    val repositorio = RepositorioRoom(BancoDeDados.criar(app))
    val armazenamento = ArmazenamentoMediaStore(app)
    val extrator = ExtratorYoutubeDl(app)
    val baixador = Baixador(extrator, armazenamento, repositorio, relogio)
}
