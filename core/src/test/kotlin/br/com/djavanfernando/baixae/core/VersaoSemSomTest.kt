package br.com.djavanfernando.baixae.core

import br.com.djavanfernando.baixae.core.fakes.Cenario
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class VersaoSemSomTest {
    private val cenario = Cenario()
    private val versaoSemSom = cenario.versaoSemSom

    private suspend fun baixar(): Video {
        cenario.acervo.iniciar()
        cenario.extrator.cadastrar("https://youtu.be/aaaaaaa", umPost(autor = "Ana", legenda = "Receita"))
        val video = cenario.baixador.baixar("https://youtu.be/aaaaaaa", "Movies/SVD").single()
        val receitas = cenario.acervo.criarBiblioteca("Receitas")
        cenario.acervo.mover(listOf(video.id), receitas.id)
        return cenario.acervo.video(video.id)!!
    }

    @Test
    fun `cria um novo Video sem audio na mesma Biblioteca, herdando os dados do original`() = runTest {
        val original = baixar()
        val conteudoOriginal = cenario.armazenamento.arquivos[original.caminhoDoArquivo]
        cenario.relogio.agora = cenario.relogio.agora.plusSeconds(60)

        val semSom = versaoSemSom.criar(original.id)

        assertEquals("${original.nome} (sem som)", semSom.nome)
        assertEquals("Movies/SVD/${original.nome} (sem som).mp4", semSom.caminhoDoArquivo)
        assertEquals("sem som($conteudoOriginal)", cenario.armazenamento.arquivos[semSom.caminhoDoArquivo])
        assertEquals(original.bibliotecaId, semSom.bibliotecaId)
        assertEquals(original.plataforma, semSom.plataforma)
        assertEquals(original.autor, semSom.autor)
        assertEquals(original.legenda, semSom.legenda)
        assertEquals(original.link, semSom.link)
        assertEquals(original.id, semSom.origemId)
        assertEquals(cenario.relogio.agora, semSom.dataDoDownload)
        assertEquals(semSom, cenario.acervo.video(semSom.id))
        assertEquals(emptyMap(), cenario.armazenamento.temporarios)
    }

    @Test
    fun `o Video original fica intacto`() = runTest {
        val original = baixar()
        val conteudo = cenario.armazenamento.arquivos[original.caminhoDoArquivo]

        versaoSemSom.criar(original.id)

        assertEquals(original, cenario.acervo.video(original.id))
        assertEquals(conteudo, cenario.armazenamento.arquivos[original.caminhoDoArquivo])
    }

    @Test
    fun `nome repetido recebe sufixo`() = runTest {
        val original = baixar()

        val primeira = versaoSemSom.criar(original.id)
        val segunda = versaoSemSom.criar(original.id)

        assertEquals("${original.nome} (sem som)", primeira.nome)
        assertEquals("${original.nome} (sem som)_2", segunda.nome)
    }

    @Test
    fun `Arquivo ausente nao gera versao sem som`() = runTest {
        val original = baixar()
        cenario.armazenamento.arquivos.clear()

        assertFailsWith<ArquivoAusente> { versaoSemSom.criar(original.id) }
        assertEquals(1, cenario.repositorio.videos().size)
    }
}
