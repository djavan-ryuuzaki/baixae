package br.com.djavanfernando.socialvideodownloader

import android.app.Application
import br.com.djavanfernando.socialvideodownloader.adaptadores.ArmazenamentoMediaStore
import br.com.djavanfernando.socialvideodownloader.adaptadores.ConectividadeAndroid
import br.com.djavanfernando.socialvideodownloader.adaptadores.ExtratorYoutubeDl
import br.com.djavanfernando.socialvideodownloader.adaptadores.PreferenciasAndroid
import br.com.djavanfernando.socialvideodownloader.core.Acervo
import br.com.djavanfernando.socialvideodownloader.core.Baixador
import br.com.djavanfernando.socialvideodownloader.core.EstadoNaFila
import br.com.djavanfernando.socialvideodownloader.core.FilaDeDownloads
import br.com.djavanfernando.socialvideodownloader.core.portas.Relogio
import br.com.djavanfernando.socialvideodownloader.dados.BancoDeDados
import br.com.djavanfernando.socialvideodownloader.dados.RepositorioRoom
import br.com.djavanfernando.socialvideodownloader.fila.AgendadorDaFila
import br.com.djavanfernando.socialvideodownloader.fila.Notificacoes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

class SocialVideoDownloaderApp : Application() {
    lateinit var dependencias: Dependencias
        private set

    override fun onCreate() {
        super.onCreate()
        dependencias = Dependencias(this)
        Notificacoes.criarCanais(this)
        dependencias.escopo.launch {
            dependencias.acervo.iniciar()
            dependencias.fila.retomarInterrompidos()
            val pendentes = dependencias.repositorio.itensDaFila().any { it.estado == EstadoNaFila.PENDENTE }
            if (pendentes) AgendadorDaFila.iniciar(this@SocialVideoDownloaderApp)
        }
    }
}

/** Liga o núcleo aos adaptadores reais. */
class Dependencias(app: Application) {
    /** Escopo de trabalho que vive enquanto o processo do app viver. */
    val escopo = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val relogio = object : Relogio {
        override fun agora(): Instant = Instant.now()
        override val fuso: ZoneId get() = ZoneId.systemDefault()
    }
    val repositorio = RepositorioRoom(BancoDeDados.criar(app))
    val preferencias = PreferenciasAndroid(app)
    val conectividade = ConectividadeAndroid(app)
    val armazenamento = ArmazenamentoMediaStore(app)
    val extrator = ExtratorYoutubeDl(app)
    val acervo = Acervo(repositorio, armazenamento)
    val baixador = Baixador(extrator, armazenamento, repositorio, relogio)
    val fila = FilaDeDownloads(repositorio, baixador, conectividade, preferencias, relogio)
}
