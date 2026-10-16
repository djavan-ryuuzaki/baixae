package br.com.djavanfernando.socialvideodownloader.core

import br.com.djavanfernando.socialvideodownloader.core.portas.ArmazenamentoDeArquivos
import br.com.djavanfernando.socialvideodownloader.core.portas.Repositorio

/** Arquivo a enviar para outro app. */
data class ArquivoCompartilhado(val caminho: String, val copia: Boolean)

class ArquivoAusente(videoId: String) : IllegalStateException("O arquivo do Vídeo $videoId não existe mais")

/** Compartilhamento: decide qual arquivo de um Vídeo é enviado a outro app. */
class Compartilhamento(
    private val repositorio: Repositorio,
    private val armazenamento: ArmazenamentoDeArquivos,
) {
    suspend fun preparar(videoId: String): ArquivoCompartilhado {
        val video = repositorio.video(videoId) ?: throw VideoNaoEncontrado(videoId)
        if (!armazenamento.existe(video.caminhoDoArquivo)) {
            repositorio.salvarVideo(video.copy(arquivoAusente = true))
            throw ArquivoAusente(videoId)
        }
        return ArquivoCompartilhado(video.caminhoDoArquivo, copia = false)
    }
}
