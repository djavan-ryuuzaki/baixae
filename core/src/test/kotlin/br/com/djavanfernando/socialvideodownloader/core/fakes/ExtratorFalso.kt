package br.com.djavanfernando.socialvideodownloader.core.fakes

import br.com.djavanfernando.socialvideodownloader.core.LimiteDeQualidade
import br.com.djavanfernando.socialvideodownloader.core.Post
import br.com.djavanfernando.socialvideodownloader.core.portas.ArquivoTemporario
import br.com.djavanfernando.socialvideodownloader.core.portas.Extrator

/** Extrator controlável: devolve Posts cadastrados e "baixa" escrevendo bytes no arquivo temporário. */
class ExtratorFalso(private val armazenamento: ArmazenamentoFalso) : Extrator {
    private val posts = mutableMapOf<String, Post>()
    val downloads = mutableListOf<Pair<String, Int>>()

    fun cadastrar(link: String, post: Post) {
        posts[link] = post
    }

    override suspend fun obterPost(link: String): Post =
        posts[link] ?: error("Post não cadastrado: $link")

    override suspend fun baixar(
        link: String,
        indice: Int,
        destino: ArquivoTemporario,
        limite: LimiteDeQualidade,
        aoProgredir: (Float) -> Unit,
    ) {
        downloads += link to indice
        aoProgredir(0.5f)
        armazenamento.escreverTemporario(destino, "conteudo de $link #$indice")
        aoProgredir(1f)
    }
}
