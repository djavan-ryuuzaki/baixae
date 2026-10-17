package br.com.djavanfernando.baixae.core

import br.com.djavanfernando.baixae.core.portas.ArmazenamentoDeArquivos
import br.com.djavanfernando.baixae.core.portas.ArquivoTemporario
import br.com.djavanfernando.baixae.core.portas.Preferencias
import br.com.djavanfernando.baixae.core.portas.ProcessadorDeVideo
import br.com.djavanfernando.baixae.core.portas.Repositorio
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Arquivo a enviar para outro app; [copia] indica uma cópia temporária (com Marca d'água). */
data class ArquivoCompartilhado(val caminho: String, val copia: Boolean)

class ArquivoAusente(videoId: String) : IllegalStateException("O arquivo do Vídeo $videoId não existe mais")

/**
 * Compartilhamento: decide qual arquivo de um Vídeo é enviado a outro app. Com a Marca d'água ligada,
 * envia uma cópia marcada; o arquivo da Biblioteca nunca é alterado.
 */
class Compartilhamento(
    private val repositorio: Repositorio,
    private val armazenamento: ArmazenamentoDeArquivos,
    private val preferencias: Preferencias,
    private val processador: ProcessadorDeVideo,
) {
    private val trava = Mutex()

    /** Cópia entregue no último Compartilhamento; o app de destino pode ainda estar lendo, então só é limpa depois. */
    private var copiaAtual: ArquivoTemporario? = null

    suspend fun preparar(videoId: String, aoProgredir: (Float) -> Unit = {}): ArquivoCompartilhado {
        val video = repositorio.video(videoId) ?: throw VideoNaoEncontrado(videoId)
        if (!armazenamento.existe(video.caminhoDoArquivo)) {
            repositorio.salvarVideo(video.copy(arquivoAusente = true))
            throw ArquivoAusente(videoId)
        }
        encerrar()
        if (!preferencias.ler().marcaDagua) return ArquivoCompartilhado(video.caminhoDoArquivo, copia = false)

        val copia = armazenamento.criarTemporario()
        try {
            processador.aplicarMarcaDagua(video.caminhoDoArquivo, copia, TEXTO_DA_MARCA_DAGUA, aoProgredir)
        } catch (erro: Throwable) {
            armazenamento.descartarTemporario(copia)
            throw erro
        }
        trava.withLock { copiaAtual = copia }
        return ArquivoCompartilhado(copia.caminho, copia = true)
    }

    /** Descarta a cópia temporária do último Compartilhamento, se houver. */
    suspend fun encerrar() = trava.withLock {
        copiaAtual?.let(armazenamento::descartarTemporario)
        copiaAtual = null
    }

    companion object {
        const val TEXTO_DA_MARCA_DAGUA = "Baixado com Baixaê"
    }
}
