package br.com.djavanfernando.baixae.core

import br.com.djavanfernando.baixae.core.fakes.Cenario
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CompartilhamentoTest {
    private val cenario = Cenario()
    private val compartilhamento = cenario.compartilhamento

    private suspend fun baixar(): Video {
        cenario.extrator.cadastrar("https://youtu.be/aaaaaaa", umPost())
        return cenario.baixador.baixar("https://youtu.be/aaaaaaa", "Movies").single()
    }

    @Test
    fun `compartilha o proprio arquivo do Video`() = runTest {
        val video = baixar()

        val arquivo = compartilhamento.preparar(video.id)

        assertEquals(ArquivoCompartilhado(video.caminhoDoArquivo, copia = false), arquivo)
    }

    @Test
    fun `Arquivo ausente nao pode ser compartilhado`() = runTest {
        val video = baixar()
        cenario.armazenamento.arquivos.clear()

        assertFailsWith<ArquivoAusente> { compartilhamento.preparar(video.id) }
        assertEquals(true, cenario.acervo.video(video.id)?.arquivoAusente)
    }
}
