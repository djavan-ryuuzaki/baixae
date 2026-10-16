package br.com.djavanfernando.socialvideodownloader.adaptadores

import android.content.Context
import android.graphics.Color
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.StaticOverlaySettings
import androidx.media3.effect.TextOverlay
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import br.com.djavanfernando.socialvideodownloader.core.portas.ArquivoTemporario
import br.com.djavanfernando.socialvideodownloader.core.portas.ProcessadorDeVideo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer

@OptIn(UnstableApi::class)
class ProcessadorAndroid(private val context: Context) : ProcessadorDeVideo {

    override suspend fun aplicarMarcaDagua(
        origem: String,
        destino: ArquivoTemporario,
        texto: String,
        aoProgredir: (Float) -> Unit,
    ) {
        val tamanhoDoTexto = tamanhoDoTexto(origem)
        aplicarMarcaDaguaNoTransformer(origem, destino, texto, tamanhoDoTexto, aoProgredir)
    }

    private suspend fun aplicarMarcaDaguaNoTransformer(
        origem: String,
        destino: ArquivoTemporario,
        texto: String,
        tamanhoDoTexto: Int,
        aoProgredir: (Float) -> Unit,
    ) = withContext(Dispatchers.Main) {
        // O Transformer precisa ser usado a partir de uma thread com Looper.
        coroutineScope {
            val resultado = CompletableDeferred<Unit>()
            val transformer = Transformer.Builder(context)
                .setVideoMimeType(MimeTypes.VIDEO_H264)
                .addListener(object : Transformer.Listener {
                    override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                        resultado.complete(Unit)
                    }

                    override fun onError(composition: Composition, exportResult: ExportResult, erro: ExportException) {
                        resultado.completeExceptionally(erro)
                    }
                })
                .build()
            val item = EditedMediaItem.Builder(MediaItem.fromUri(Uri.parse(origem)))
                .setEffects(Effects(emptyList(), listOf(OverlayEffect(listOf(marcaDagua(texto, tamanhoDoTexto))))))
                .build()
            transformer.start(item, destino.caminho)
            val sondagem = launch {
                val progresso = ProgressHolder()
                while (isActive) {
                    if (transformer.getProgress(progresso) == Transformer.PROGRESS_STATE_AVAILABLE) {
                        aoProgredir(progresso.progress / 100f)
                    }
                    delay(INTERVALO_DE_PROGRESSO_MS)
                }
            }
            try {
                resultado.await()
                aoProgredir(1f)
            } finally {
                sondagem.cancel()
                if (!resultado.isCompleted) transformer.cancel()
            }
        }
    }

    /**
     * O TextOverlay desenha o texto num tamanho fixo em pixels, sem acompanhar a resolução do vídeo;
     * por isso o tamanho é calculado a partir do lado menor do quadro.
     */
    private suspend fun tamanhoDoTexto(origem: String): Int = withContext(Dispatchers.IO) {
        val leitor = MediaMetadataRetriever()
        try {
            leitor.setDataSource(context, Uri.parse(origem))
            val largura = leitor.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            val altura = leitor.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            (minOf(largura, altura) * PROPORCAO_DO_TEXTO).toInt().coerceAtLeast(TAMANHO_MINIMO_DO_TEXTO)
        } catch (_: Exception) {
            TAMANHO_MINIMO_DO_TEXTO
        } finally {
            leitor.release()
        }
    }

    /** Texto branco sobre fundo escuro translúcido, no canto inferior direito. */
    private fun marcaDagua(texto: String, tamanho: Int): TextOverlay {
        val estilizado = SpannableString(" $texto ").apply {
            setSpan(AbsoluteSizeSpan(tamanho), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(Color.WHITE), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(BackgroundColorSpan(Color.argb(110, 0, 0, 0)), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        val posicao = StaticOverlaySettings.Builder()
            .setOverlayFrameAnchor(1f, -1f)
            .setBackgroundFrameAnchor(0.96f, -0.94f)
            .setAlphaScale(0.7f)
            .build()
        return TextOverlay.createStaticTextOverlay(estilizado, posicao)
    }

    override suspend fun removerAudio(origem: String, destino: ArquivoTemporario) = withContext(Dispatchers.IO) {
        val extrator = MediaExtractor()
        var muxer: MediaMuxer? = null
        try {
            extrator.setDataSource(context, Uri.parse(origem), null)
            val trilha = (0 until extrator.trackCount).firstOrNull { indice ->
                extrator.getTrackFormat(indice).getString(MediaFormat.KEY_MIME)?.startsWith("video/") == true
            } ?: error("O arquivo não tem trilha de vídeo")
            val formato = extrator.getTrackFormat(trilha)
            extrator.selectTrack(trilha)
            muxer = MediaMuxer(destino.caminho, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            if (formato.containsKey(MediaFormat.KEY_ROTATION)) muxer.setOrientationHint(formato.getInteger(MediaFormat.KEY_ROTATION))
            val trilhaDeSaida = muxer.addTrack(formato)
            muxer.start()
            val tamanho = if (formato.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) formato.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE) else 0
            val buffer = ByteBuffer.allocate(maxOf(tamanho, TAMANHO_MINIMO_DO_BUFFER))
            val info = MediaCodec.BufferInfo()
            while (true) {
                ensureActive()
                info.size = extrator.readSampleData(buffer, 0)
                if (info.size < 0) break
                info.offset = 0
                info.presentationTimeUs = extrator.sampleTime
                info.flags = if (extrator.sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC != 0) MediaCodec.BUFFER_FLAG_KEY_FRAME else 0
                muxer.writeSampleData(trilhaDeSaida, buffer, info)
                extrator.advance()
            }
            muxer.stop()
        } finally {
            muxer?.release()
            extrator.release()
        }
    }

    private companion object {
        const val INTERVALO_DE_PROGRESSO_MS = 300L
        const val TAMANHO_MINIMO_DO_BUFFER = 4 * 1024 * 1024
        const val PROPORCAO_DO_TEXTO = 0.045f
        const val TAMANHO_MINIMO_DO_TEXTO = 10
    }
}
