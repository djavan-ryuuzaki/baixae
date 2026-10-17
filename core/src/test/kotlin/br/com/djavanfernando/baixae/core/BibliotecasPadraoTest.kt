package br.com.djavanfernando.baixae.core

import br.com.djavanfernando.baixae.core.fakes.Cenario
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BibliotecasPadraoTest {
    private val cenario = Cenario()

    @Test
    fun `na primeira execucao existe uma Biblioteca padrao por Plataforma`() = runTest {
        cenario.acervo.iniciar()

        val bibliotecas = cenario.acervo.bibliotecas()
        assertEquals(Plataforma.entries.toSet(), bibliotecas.mapNotNull { it.plataforma }.toSet())
        assertEquals(5, bibliotecas.size)
        assertTrue(bibliotecas.all { it.padrao })
        assertEquals("Instagram", bibliotecas.single { it.plataforma == Plataforma.INSTAGRAM }.nome)
    }

    @Test
    fun `iniciar de novo nao duplica as Bibliotecas padrao`() = runTest {
        cenario.acervo.iniciar()
        cenario.acervo.iniciar()

        assertEquals(5, cenario.acervo.bibliotecas().size)
    }

    @Test
    fun `Video baixado vai para a Biblioteca padrao da sua Plataforma com os metadados do Post`() = runTest {
        cenario.acervo.iniciar()
        val link = "https://www.instagram.com/reel/abc/"
        cenario.extrator.cadastrar(link, umPost(autor = "chef", legenda = "Bolo de cenoura"))

        val video = cenario.baixador.baixar(link, "Movies/Baixaê").single()

        val instagram = cenario.acervo.bibliotecas().single { it.plataforma == Plataforma.INSTAGRAM }
        assertEquals(listOf(video), cenario.acervo.videosDa(instagram.id))
        assertEquals("chef", video.autor)
        assertEquals("Bolo de cenoura", video.legenda)
        assertEquals(30, video.duracaoEmSegundos)
        assertEquals("https://exemplo/miniatura.jpg", video.miniatura)
        assertEquals(cenario.relogio.agora, video.dataDoDownload)
        val youtube = cenario.acervo.bibliotecas().single { it.plataforma == Plataforma.YOUTUBE }
        assertEquals(emptyList(), cenario.acervo.videosDa(youtube.id))
    }
}
