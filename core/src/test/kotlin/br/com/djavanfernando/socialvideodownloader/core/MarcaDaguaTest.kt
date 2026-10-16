package br.com.djavanfernando.socialvideodownloader.core

import br.com.djavanfernando.socialvideodownloader.core.fakes.Cenario
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MarcaDaguaTest {
    private val cenario = Cenario()
    private val compartilhamento = cenario.compartilhamento

    private suspend fun baixar(): Video {
        cenario.extrator.cadastrar("https://youtu.be/aaaaaaa", umPost())
        return cenario.baixador.baixar("https://youtu.be/aaaaaaa", "Movies").single()
    }

    private suspend fun ligarMarcaDagua() = cenario.preferencias.salvar(Configuracoes(marcaDagua = true))

    @Test
    fun `Marca d'agua desligada por padrao compartilha o arquivo original`() = runTest {
        val video = baixar()

        val arquivo = compartilhamento.preparar(video.id)

        assertEquals(false, Configuracoes().marcaDagua)
        assertEquals(ArquivoCompartilhado(video.caminhoDoArquivo, copia = false), arquivo)
        assertEquals(emptyList(), cenario.processador.textosAplicados)
    }

    @Test
    fun `Marca d'agua ligada compartilha uma copia marcada e o arquivo da Biblioteca fica identico`() = runTest {
        val video = baixar()
        val original = cenario.armazenamento.arquivos[video.caminhoDoArquivo]
        ligarMarcaDagua()

        val arquivo = compartilhamento.preparar(video.id)

        assertTrue(arquivo.copia)
        assertEquals("marcado($original)", cenario.armazenamento.temporarios[arquivo.caminho])
        assertEquals(listOf("Baixado com SocialVideoDownloader"), cenario.processador.textosAplicados)
        assertEquals(original, cenario.armazenamento.arquivos[video.caminhoDoArquivo])
        assertEquals(setOf(video.caminhoDoArquivo), cenario.armazenamento.arquivos.keys)
    }

    @Test
    fun `progresso da geracao e informado e cancelar descarta a copia`() = runTest {
        val video = baixar()
        ligarMarcaDagua()
        cenario.processador.trava = CompletableDeferred()
        val progresso = mutableListOf<Float>()

        val preparo = async { compartilhamento.preparar(video.id) { progresso += it } }
        runCurrent()
        assertEquals(listOf(0.5f), progresso)
        assertEquals(1, cenario.armazenamento.temporarios.size)

        preparo.cancel()
        runCurrent()

        assertEquals(emptyMap(), cenario.armazenamento.temporarios)
    }

    @Test
    fun `copias temporarias sao limpas no proximo Compartilhamento e ao encerrar`() = runTest {
        val video = baixar()
        ligarMarcaDagua()

        val primeira = compartilhamento.preparar(video.id)
        val segunda = compartilhamento.preparar(video.id)
        assertEquals(setOf(segunda.caminho), cenario.armazenamento.temporarios.keys)
        assertTrue(primeira.caminho != segunda.caminho)

        compartilhamento.encerrar()
        assertEquals(emptyMap(), cenario.armazenamento.temporarios)
    }
}
