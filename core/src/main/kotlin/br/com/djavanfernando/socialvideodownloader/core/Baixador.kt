package br.com.djavanfernando.socialvideodownloader.core

import br.com.djavanfernando.socialvideodownloader.core.portas.ArmazenamentoDeArquivos
import br.com.djavanfernando.socialvideodownloader.core.portas.ArquivoTemporario
import br.com.djavanfernando.socialvideodownloader.core.portas.Extrator
import br.com.djavanfernando.socialvideodownloader.core.portas.Relogio
import br.com.djavanfernando.socialvideodownloader.core.portas.Repositorio
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/** Executa um Download: obtém o Post pelo Link, baixa seus vídeos e os registra como Vídeos. */
class Baixador(
    private val extrator: Extrator,
    private val armazenamento: ArmazenamentoDeArquivos,
    private val repositorio: Repositorio,
    private val relogio: Relogio,
) {
    private val travaDeNomes = Mutex()

    suspend fun baixar(
        link: String,
        pasta: String,
        limite: LimiteDeQualidade = LimiteDeQualidade.P1080,
        aoObterPost: suspend (Post) -> Unit = {},
        aoProgredir: (Float) -> Unit = {},
    ): List<Video> {
        val plataforma = Plataforma.reconhecer(link) ?: throw LinkNaoSuportado(link)
        val post = extrator.obterPost(link)
        aoObterPost(post)
        val agora = relogio.agora()
        val midias = post.midias.filter { it.tipo == TipoDeMidia.VIDEO }
        return midias.mapIndexed { posicao, midia ->
            val temporario = armazenamento.criarTemporario()
            try {
                extrator.baixar(link, midia.indice, temporario, limite) { aoProgredir((posicao + it) / midias.size) }
                val base = NomeDeVideo.padrao(plataforma, post.autor, agora.atZone(relogio.fuso).toLocalDate())
                val (nome, caminho) = salvarComNomeLivre(temporario, base, pasta)
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

    /** Escolhe o nome e salva sob a mesma trava, para que Downloads simultâneos não disputem o mesmo nome. */
    private suspend fun salvarComNomeLivre(temporario: ArquivoTemporario, base: String, pasta: String) =
        travaDeNomes.withLock {
            val nome = NomeDeVideo.livre(base, pasta, armazenamento)
            nome to armazenamento.salvar(temporario, pasta, "$nome.${Video.EXTENSAO}")
        }
}
