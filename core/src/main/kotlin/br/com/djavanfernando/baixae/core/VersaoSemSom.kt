package br.com.djavanfernando.baixae.core

import br.com.djavanfernando.baixae.core.portas.ArmazenamentoDeArquivos
import br.com.djavanfernando.baixae.core.portas.ProcessadorDeVideo
import br.com.djavanfernando.baixae.core.portas.Relogio
import br.com.djavanfernando.baixae.core.portas.Repositorio
import java.util.UUID

/** Cria a Versão sem som de um Vídeo: um novo Vídeo na mesma Biblioteca, sem o áudio original. */
class VersaoSemSom(
    private val repositorio: Repositorio,
    private val armazenamento: ArmazenamentoDeArquivos,
    private val processador: ProcessadorDeVideo,
    private val relogio: Relogio,
) {
    suspend fun criar(videoId: String): Video {
        val original = repositorio.video(videoId) ?: throw VideoNaoEncontrado(videoId)
        if (!armazenamento.existe(original.caminhoDoArquivo)) {
            repositorio.salvarVideo(original.copy(arquivoAusente = true))
            throw ArquivoAusente(videoId)
        }
        val temporario = armazenamento.criarTemporario()
        try {
            processador.removerAudio(original.caminhoDoArquivo, temporario)
            val (nome, caminho) = NomeDeVideo.salvarComNomeLivre(
                armazenamento,
                temporario,
                base = "${original.nome} (sem som)",
                pasta = original.pasta,
            )
            return original.copy(
                id = UUID.randomUUID().toString(),
                nome = nome,
                caminhoDoArquivo = caminho,
                dataDoDownload = relogio.agora(),
                arquivoAusente = false,
                origemId = original.id,
            ).also { repositorio.salvarVideo(it) }
        } finally {
            armazenamento.descartarTemporario(temporario)
        }
    }
}
