package br.com.djavanfernando.socialvideodownloader.adaptadores

import android.content.Context
import br.com.djavanfernando.socialvideodownloader.core.FalhaDoExtrator
import br.com.djavanfernando.socialvideodownloader.core.LimiteDeQualidade
import br.com.djavanfernando.socialvideodownloader.core.MidiaDoPost
import br.com.djavanfernando.socialvideodownloader.core.Post
import br.com.djavanfernando.socialvideodownloader.core.TipoDeMidia
import br.com.djavanfernando.socialvideodownloader.core.portas.ArquivoTemporario
import br.com.djavanfernando.socialvideodownloader.core.portas.Extrator
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.YoutubeDLResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.util.UUID

/** Extrator baseado no youtubedl-android (yt-dlp + Python + ffmpeg + QuickJS embutidos). */
class ExtratorYoutubeDl(private val context: Context) : Extrator {
    private val inicializacao = Mutex()
    private var inicializado = false

    private suspend fun garantirInicializado() = inicializacao.withLock {
        if (!inicializado) {
            withContext(Dispatchers.IO) {
                YoutubeDL.getInstance().init(context)
                FFmpeg.getInstance().init(context)
            }
            inicializado = true
        }
    }

    override suspend fun obterPost(link: String): Post {
        garantirInicializado()
        val pedido = YoutubeDLRequest(link).apply {
            addOption("--dump-single-json")
            addOption("--no-playlist")
            addOption("--no-warnings")
        }
        val json = JSONObject(executar(pedido) { _ -> }.out)
        val entradas = json.optJSONArray("entries")
        val midias = if (entradas == null) {
            listOf(midia(0, json))
        } else {
            List(entradas.length()) { midia(it, entradas.getJSONObject(it)) }
        }
        val primeira = entradas?.optJSONObject(0) ?: json
        return Post(
            autor = texto(json, "uploader", "channel", "uploader_id")
                ?: texto(primeira, "uploader", "channel", "uploader_id") ?: "desconhecido",
            legenda = legenda(json) ?: legenda(primeira) ?: "",
            duracaoEmSegundos = primeira.optDouble("duration", 0.0).toLong(),
            miniatura = texto(json, "thumbnail") ?: texto(primeira, "thumbnail"),
            midias = midias,
        )
    }

    override suspend fun baixar(
        link: String,
        indice: Int,
        destino: ArquivoTemporario,
        limite: LimiteDeQualidade,
        aoProgredir: (Float) -> Unit,
    ) {
        garantirInicializado()
        val arquivoFinal = File(destino.caminho)
        val altura = limite.alturaMaxima
        val formato = if (altura == null) {
            "bv*+ba/b"
        } else {
            "bv*[height<=$altura]+ba/b[height<=$altura]/bv*+ba/b"
        }
        val pedido = YoutubeDLRequest(link).apply {
            addOption("-f", formato)
            addOption("-S", "vcodec:h264,res,acodec:m4a")
            addOption("--merge-output-format", "mp4")
            addOption("--remux-video", "mp4")
            addOption("--no-playlist")
            addOption("--playlist-items", (indice + 1).toString())
            addOption("--no-mtime")
            addOption("--no-warnings")
            addOption("-o", File(arquivoFinal.parentFile, arquivoFinal.nameWithoutExtension).absolutePath + ".%(ext)s")
        }
        executar(pedido) { progresso -> aoProgredir((progresso / 100f).coerceIn(0f, 1f)) }
        if (!arquivoFinal.exists()) error("O Extrator não gerou o arquivo esperado")
    }

    /** Executa o yt-dlp; o cancelamento da coroutine encerra o processo. */
    private suspend fun executar(
        pedido: YoutubeDLRequest,
        aoProgredir: (Float) -> Unit,
    ): YoutubeDLResponse = coroutineScope {
        val idDoProcesso = UUID.randomUUID().toString()
        val vigia = launch {
            try {
                awaitCancellation()
            } finally {
                YoutubeDL.getInstance().destroyProcessById(idDoProcesso)
            }
        }
        try {
            runInterruptible(Dispatchers.IO) {
                YoutubeDL.getInstance().execute(pedido, idDoProcesso) { progresso, _, _ -> aoProgredir(progresso) }
            }
        } catch (erro: YoutubeDLException) {
            throw FalhaDoExtrator(ClassificadorDeFalhas.classificar(erro.message.orEmpty()), erro.message)
        } finally {
            vigia.cancel()
        }
    }

    override suspend fun atualizar(): String {
        garantirInicializado()
        try {
            withContext(Dispatchers.IO) {
                YoutubeDL.getInstance().updateYoutubeDL(context, YoutubeDL.UpdateChannel.STABLE)
            }
        } catch (erro: YoutubeDLException) {
            throw FalhaDoExtrator(ClassificadorDeFalhas.classificar(erro.message.orEmpty()), erro.message)
        }
        return versao()
    }

    override suspend fun versao(): String {
        garantirInicializado()
        return withContext(Dispatchers.IO) { YoutubeDL.getInstance().version(context) }.orEmpty()
    }

    private fun midia(indice: Int, json: JSONObject): MidiaDoPost {
        val semVideo = json.optString("vcodec") == "none" ||
            json.optString("ext") in setOf("jpg", "jpeg", "png", "webp", "heic")
        return MidiaDoPost(
            indice = indice,
            tipo = if (semVideo) TipoDeMidia.FOTO else TipoDeMidia.VIDEO,
            tamanhoEstimadoEmBytes = tamanho(json),
        )
    }

    private fun tamanho(json: JSONObject): Long? {
        json.optLong("filesize").takeIf { it > 0 }?.let { return it }
        json.optLong("filesize_approx").takeIf { it > 0 }?.let { return it }
        val formatos = json.optJSONArray("requested_formats") ?: return null
        return (0 until formatos.length()).sumOf { i ->
            val formato = formatos.getJSONObject(i)
            formato.optLong("filesize").takeIf { it > 0 } ?: formato.optLong("filesize_approx")
        }.takeIf { it > 0 }
    }

    private fun legenda(json: JSONObject): String? {
        val titulo = texto(json, "title")
        if (json.optString("extractor_key").startsWith("Youtube")) return titulo
        return texto(json, "description") ?: titulo
    }

    private fun texto(json: JSONObject, vararg chaves: String): String? =
        chaves.firstNotNullOfOrNull { chave -> json.optString(chave).takeIf { it.isNotBlank() && it != "null" } }
}
