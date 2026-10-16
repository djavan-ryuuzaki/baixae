package br.com.djavanfernando.socialvideodownloader.core

import br.com.djavanfernando.socialvideodownloader.core.fakes.Cenario
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class RenomearEPastaTest {
    private val cenario = Cenario()

    private suspend fun baixar(link: String = "https://x.com/a/status/1", pasta: String = "Movies/SVD"): Video {
        cenario.extrator.cadastrar(link, umPost(autor = "Maria"))
        return cenario.baixador.baixar(link, pasta).single()
    }

    @Test
    fun `renomear um Video renomeia o arquivo junto`() = runTest {
        val video = baixar()

        val renomeado = cenario.acervo.renomear(video.id, "Receita de bolo")

        assertEquals("Receita de bolo", renomeado.nome)
        assertEquals("Movies/SVD/Receita de bolo.mp4", renomeado.caminhoDoArquivo)
        assertEquals(setOf("Movies/SVD/Receita de bolo.mp4"), cenario.armazenamento.arquivos.keys)
        assertEquals(renomeado, cenario.acervo.video(video.id))
    }

    @Test
    fun `renomear sanitiza caracteres invalidos`() = runTest {
        val video = baixar()

        assertEquals("a_b", cenario.acervo.renomear(video.id, " a/b ").nome)
    }

    @Test
    fun `renomear para um nome ja existente na pasta e recusado`() = runTest {
        val primeiro = baixar()
        val segundo = baixar()

        assertFailsWith<NomeJaExiste> { cenario.acervo.renomear(segundo.id, primeiro.nome) }

        assertEquals(segundo, cenario.acervo.video(segundo.id))
        assertEquals(2, cenario.armazenamento.arquivos.size)
    }

    @Test
    fun `renomear para um nome vazio e recusado`() = runTest {
        val video = baixar()

        assertFailsWith<NomeInvalido> { cenario.acervo.renomear(video.id, " / ") }
    }

    @Test
    fun `renomear para o mesmo nome nao faz nada`() = runTest {
        val video = baixar()

        assertEquals(video, cenario.acervo.renomear(video.id, video.nome))
    }

    @Test
    fun `Download sem pasta usa a Pasta de destino padrao das Configuracoes`() = runTest {
        cenario.preferencias.salvar(Configuracoes(pastaPadrao = "DCIM/Videos baixados"))
        cenario.extrator.cadastrar("https://x.com/a/status/1", umPost(autor = "Maria"))

        cenario.fila.enfileirar("https://x.com/a/status/1")
        cenario.fila.processar()

        assertEquals("DCIM/Videos baixados", cenario.repositorio.videos().single().pasta)
    }

    @Test
    fun `Download pode usar outra Pasta de destino so para ele`() = runTest {
        cenario.extrator.cadastrar("https://x.com/a/status/1", umPost(autor = "Maria"))

        cenario.fila.enfileirar("https://x.com/a/status/1", pasta = "Movies/Excecao")
        cenario.fila.processar()

        assertEquals("Movies/Excecao", cenario.repositorio.videos().single().pasta)
        assertEquals(Configuracoes.PASTA_PADRAO, cenario.preferencias.ler().pastaPadrao)
    }

    @Test
    fun `Pasta de destino precisa estar em Movies, DCIM ou Pictures`() {
        assertEquals("Movies/SocialVideoDownloader", PastaDeDestino.validar(" /Movies//SocialVideoDownloader/ "))
        assertEquals("DCIM", PastaDeDestino.validar("dcim"))
        assertEquals("Pictures/Receitas/Doces", PastaDeDestino.validar("Pictures/Receitas/Doces"))
        assertEquals("Movies/a_b", PastaDeDestino.validar("Movies/a:b"))
        assertNull(PastaDeDestino.validar("Download/videos"))
        assertNull(PastaDeDestino.validar(""))
        assertNull(PastaDeDestino.validar("Movies/../Android/data"))
    }
}
