package br.com.djavanfernando.baixae.core

import br.com.djavanfernando.baixae.core.fakes.Cenario
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ApagarEArquivoAusenteTest {
    private val cenario = Cenario()
    private val acervo = cenario.acervo

    private suspend fun baixar(link: String = "https://youtu.be/aaaaaaa"): Video {
        cenario.extrator.cadastrar(link, umPost())
        return cenario.baixador.baixar(link, "Movies").single()
    }

    @Test
    fun `apagar Video com o arquivo remove os dois`() = runTest {
        val video = baixar()

        acervo.apagarVideo(video.id, apagarArquivo = true)

        assertNull(acervo.video(video.id))
        assertEquals(emptyMap(), cenario.armazenamento.arquivos)
    }

    @Test
    fun `apagar Video sem o arquivo so tira o Video do app`() = runTest {
        val video = baixar()

        acervo.apagarVideo(video.id, apagarArquivo = false)

        assertNull(acervo.video(video.id))
        assertEquals(setOf(video.caminhoDoArquivo), cenario.armazenamento.arquivos.keys)
    }

    @Test
    fun `apagar Video cujo arquivo ja sumiu funciona`() = runTest {
        val video = baixar()
        cenario.armazenamento.arquivos.clear()

        acervo.apagarVideo(video.id, apagarArquivo = true)

        assertNull(acervo.video(video.id))
    }

    @Test
    fun `Video cujo arquivo foi apagado fora do app fica marcado como Arquivo ausente`() = runTest {
        val sumiu = baixar("https://youtu.be/aaaaaaa")
        val ficou = baixar("https://youtu.be/bbbbbbb")
        cenario.armazenamento.arquivos.remove(sumiu.caminhoDoArquivo)

        acervo.verificarArquivos(Biblioteca.idPadrao(Plataforma.YOUTUBE))

        assertTrue(acervo.video(sumiu.id)!!.arquivoAusente)
        assertFalse(acervo.video(ficou.id)!!.arquivoAusente)
    }

    @Test
    fun `Video volta ao normal se o arquivo reaparecer`() = runTest {
        val video = baixar()
        val conteudo = cenario.armazenamento.arquivos.remove(video.caminhoDoArquivo)!!
        acervo.verificarArquivos(video.bibliotecaId)

        cenario.armazenamento.arquivos[video.caminhoDoArquivo] = conteudo
        acervo.verificarArquivos(video.bibliotecaId)

        assertFalse(acervo.video(video.id)!!.arquivoAusente)
    }
}
