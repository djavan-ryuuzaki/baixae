package br.com.djavanfernando.baixae.core.fakes

import br.com.djavanfernando.baixae.core.FalhaDoExtrator
import br.com.djavanfernando.baixae.core.LimiteDeQualidade
import br.com.djavanfernando.baixae.core.Post
import br.com.djavanfernando.baixae.core.portas.ArquivoTemporario
import br.com.djavanfernando.baixae.core.portas.Extrator
import kotlinx.coroutines.CompletableDeferred

/**
 * Extrator controlável: devolve Posts cadastrados e "baixa" escrevendo no arquivo temporário.
 * Um Link pode ser [segurar]do (o download fica em andamento até ser liberado) ou programado para falhar.
 */
class ExtratorFalso(private val armazenamento: ArmazenamentoFalso) : Extrator {
    private val posts = mutableMapOf<String, Post>()
    private val travas = mutableMapOf<String, CompletableDeferred<Unit>>()
    private val falhas = mutableMapOf<String, Throwable>()
    val downloads = mutableListOf<Pair<String, Int>>()
    val limitesPedidos = mutableListOf<LimiteDeQualidade>()
    var versao = "2026.01.01"
    var versaoAposAtualizar = "2026.10.01"
    var atualizacoes = 0

    /** Executado logo antes de uma falha programada ser lançada (ex.: derrubar a rede junto). */
    var aoFalhar: () -> Unit = {}

    fun cadastrar(link: String, post: Post) {
        posts[link] = post
    }

    /** O download de [link] fica em andamento (com o temporário já escrito pela metade) até [liberar]. */
    fun segurar(link: String) {
        travas[link] = CompletableDeferred()
    }

    fun liberar(link: String) {
        travas.remove(link)?.complete(Unit)
    }

    /** O próximo download de [link] falha com [falha]; a falha é consumida. */
    fun falhar(link: String, falha: Throwable) {
        falhas[link] = falha
    }

    override suspend fun obterPost(link: String, limite: LimiteDeQualidade): Post =
        posts[link] ?: error("Post não cadastrado: $link")

    override suspend fun baixar(
        link: String,
        indice: Int,
        destino: ArquivoTemporario,
        limite: LimiteDeQualidade,
        aoProgredir: (Float) -> Unit,
    ) {
        downloads += link to indice
        limitesPedidos += limite
        armazenamento.escreverTemporario(destino, "parcial")
        aoProgredir(0.5f)
        travas[link]?.await()
        falhas.remove(link)?.let {
            aoFalhar()
            throw it
        }
        armazenamento.escreverTemporario(destino, "conteudo de $link #$indice")
        aoProgredir(1f)
    }

    override suspend fun atualizar(): String {
        atualizacoes++
        versao = versaoAposAtualizar
        return versao
    }

    override suspend fun versao(): String = versao
}
