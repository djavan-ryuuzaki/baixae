package br.com.djavanfernando.socialvideodownloader.core

import br.com.djavanfernando.socialvideodownloader.core.portas.ArmazenamentoDeArquivos
import br.com.djavanfernando.socialvideodownloader.core.portas.Extrator
import br.com.djavanfernando.socialvideodownloader.core.portas.Relogio
import br.com.djavanfernando.socialvideodownloader.core.portas.Repositorio
import java.time.ZoneOffset
import java.util.UUID

/** Executa um Download: obtém o Post pelo Link, baixa seus vídeos e os registra como Vídeos. */
class Baixador(
    private val extrator: Extrator,
    private val armazenamento: ArmazenamentoDeArquivos,
    private val repositorio: Repositorio,
    private val relogio: Relogio,
) {
    suspend fun baixar(link: String, pasta: String): List<Video> {
        val plataforma = Plataforma.reconhecer(link) ?: throw LinkNaoSuportado(link)
        val post = extrator.obterPost(link)
        val agora = relogio.agora()
        return post.midias.filter { it.tipo == TipoDeMidia.VIDEO }.map { midia ->
            val temporario = armazenamento.criarTemporario()
            try {
                extrator.baixar(link, midia.indice, temporario, LimiteDeQualidade.P1080) {}
                val nome = "${plataforma.name.lowercase()}_${post.autor}_${agora.atOffset(ZoneOffset.UTC).toLocalDate()}"
                val caminho = armazenamento.salvar(temporario, pasta, "$nome.${Video.EXTENSAO}")
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
}
