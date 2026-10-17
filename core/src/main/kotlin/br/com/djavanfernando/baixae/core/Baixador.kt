package br.com.djavanfernando.baixae.core

import br.com.djavanfernando.baixae.core.portas.ArmazenamentoDeArquivos
import br.com.djavanfernando.baixae.core.portas.Extrator
import br.com.djavanfernando.baixae.core.portas.Relogio
import br.com.djavanfernando.baixae.core.portas.Repositorio
import java.util.UUID

/** Executa um Download: obtém o Post pelo Link, baixa seus vídeos e os registra como Vídeos. */
class Baixador(
    private val extrator: Extrator,
    private val armazenamento: ArmazenamentoDeArquivos,
    private val repositorio: Repositorio,
    private val relogio: Relogio,
) {
    suspend fun baixar(
        link: String,
        pasta: String,
        limite: LimiteDeQualidade = LimiteDeQualidade.P1080,
        aoObterPost: suspend (Post) -> Unit = {},
        aoProgredir: (Float) -> Unit = {},
    ): List<Video> {
        val plataforma = Plataforma.reconhecer(link) ?: throw LinkNaoSuportado(link)
        val post = extrator.obterPost(link, limite)
        aoObterPost(post)
        val agora = relogio.agora()
        val midias = post.midias.filter { it.tipo == TipoDeMidia.VIDEO }
        verificarEspaco(midias, pasta)
        return midias.mapIndexed { posicao, midia ->
            val temporario = armazenamento.criarTemporario()
            try {
                extrator.baixar(link, midia.indice, temporario, limite) { aoProgredir((posicao + it) / midias.size) }
                val base = NomeDeVideo.padrao(plataforma, post.autor, agora.atZone(relogio.fuso).toLocalDate())
                val (nome, caminho) = NomeDeVideo.salvarComNomeLivre(armazenamento, temporario, base, pasta)
                Video(
                    id = UUID.randomUUID().toString(),
                    nome = nome,
                    plataforma = plataforma,
                    link = link,
                    autor = post.autor,
                    legenda = post.legenda,
                    dataDoDownload = agora,
                    duracaoEmSegundos = post.duracaoEmSegundos,
                    miniatura = post.miniatura,
                    pasta = pasta,
                    caminhoDoArquivo = caminho,
                    bibliotecaId = Biblioteca.idPadrao(plataforma),
                ).also { repositorio.salvarVideo(it) }
            } finally {
                armazenamento.descartarTemporario(temporario)
            }
        }
    }

    /**
     * Falha antes de começar se o tamanho estimado não couber. Conta o dobro do estimado: o vídeo é
     * gravado primeiro num temporário e depois copiado para a Pasta de destino.
     */
    private fun verificarEspaco(midias: List<MidiaDoPost>, pasta: String) {
        val estimado = midias.sumOf { it.tamanhoEstimadoEmBytes ?: 0L }
        if (estimado <= 0) return
        if (armazenamento.espacoLivre(pasta) < estimado * 2) {
            throw FalhaDoExtrator(TipoDeFalha.SEM_ESPACO, "Espaço insuficiente: são necessários ${estimado * 2} bytes")
        }
    }
}
