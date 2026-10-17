package br.com.djavanfernando.baixae

import android.app.Application
import br.com.djavanfernando.baixae.adaptadores.ArmazenamentoMediaStore
import br.com.djavanfernando.baixae.adaptadores.ConectividadeAndroid
import br.com.djavanfernando.baixae.adaptadores.ExtratorYoutubeDl
import br.com.djavanfernando.baixae.adaptadores.PreferenciasAndroid
import br.com.djavanfernando.baixae.adaptadores.ProcessadorAndroid
import br.com.djavanfernando.baixae.adaptadores.VerificadorGithub
import br.com.djavanfernando.baixae.core.Acervo
import br.com.djavanfernando.baixae.core.AtualizacoesDoApp
import br.com.djavanfernando.baixae.core.Baixador
import br.com.djavanfernando.baixae.core.Compartilhamento
import br.com.djavanfernando.baixae.core.EstadoNaFila
import br.com.djavanfernando.baixae.core.Extratores
import br.com.djavanfernando.baixae.core.FilaDeDownloads
import br.com.djavanfernando.baixae.core.VerificacaoSemanal
import br.com.djavanfernando.baixae.core.VersaoSemSom
import br.com.djavanfernando.baixae.core.portas.Relogio
import br.com.djavanfernando.baixae.dados.BancoDeDados
import br.com.djavanfernando.baixae.dados.RepositorioRoom
import br.com.djavanfernando.baixae.fila.AgendadorDaFila
import br.com.djavanfernando.baixae.fila.Notificacoes
import br.com.djavanfernando.baixae.fila.VerificacaoSemanalWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

class BaixaeApp : Application() {
    lateinit var dependencias: Dependencias
        private set

    override fun onCreate() {
        super.onCreate()
        dependencias = Dependencias(this)
        dependencias.armazenamento.limparTemporarios()
        Notificacoes.criarCanais(this)
        VerificacaoSemanalWorker.agendar(this)
        dependencias.escopo.launch {
            dependencias.acervo.iniciar()
            dependencias.fila.retomarInterrompidos()
            val pendentes = dependencias.repositorio.itensDaFila().any { it.estado == EstadoNaFila.PENDENTE }
            if (pendentes) AgendadorDaFila.iniciar(this@BaixaeApp)
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
    val processador = ProcessadorAndroid(app)
    val compartilhamento = Compartilhamento(repositorio, armazenamento, preferencias, processador)
    val versaoSemSom = VersaoSemSom(repositorio, armazenamento, processador, relogio)
    val fila = FilaDeDownloads(repositorio, baixador, conectividade, preferencias, relogio)
    val extratores = Extratores(extrator, fila, preferencias, relogio)
    val atualizacoesDoApp = AtualizacoesDoApp(
        VerificadorGithub(BuildConfig.REPOSITORIO_GITHUB),
        preferencias,
        versaoInstalada = BuildConfig.VERSION_NAME,
    )
    val verificacaoSemanal = VerificacaoSemanal(extratores, atualizacoesDoApp)
}
