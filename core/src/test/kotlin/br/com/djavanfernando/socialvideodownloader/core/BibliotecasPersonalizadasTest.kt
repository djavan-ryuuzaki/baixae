package br.com.djavanfernando.socialvideodownloader.core

import br.com.djavanfernando.socialvideodownloader.core.fakes.Cenario
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull

class BibliotecasPersonalizadasTest {
    private val cenario = Cenario()
    private val acervo = cenario.acervo

    @BeforeTest
    fun iniciar() = runTest { acervo.iniciar() }

    private suspend fun baixar(link: String): Video {
        cenario.extrator.cadastrar(link, umPost())
        return cenario.baixador.baixar(link, "Movies").single()
    }

    @Test
    fun `criar Biblioteca personalizada`() = runTest {
        val receitas = acervo.criarBiblioteca("  Receitas ")

        assertEquals("Receitas", receitas.nome)
        assertFalse(receitas.padrao)
        assertEquals(receitas, acervo.bibliotecas().single { it.id == receitas.id })
    }

    @Test
    fun `nomes repetidos ou vazios sao recusados`() = runTest {
        acervo.criarBiblioteca("Receitas")

        assertFailsWith<BibliotecaJaExiste> { acervo.criarBiblioteca("receitas") }
        assertFailsWith<BibliotecaJaExiste> { acervo.criarBiblioteca("YouTube") }
        assertFailsWith<NomeInvalido> { acervo.criarBiblioteca("   ") }
    }

    @Test
    fun `renomear Biblioteca personalizada`() = runTest {
        val receitas = acervo.criarBiblioteca("Receitas")
        acervo.criarBiblioteca("Humor")

        assertEquals("Doces", acervo.renomearBiblioteca(receitas.id, "Doces").nome)
        assertFailsWith<BibliotecaJaExiste> { acervo.renomearBiblioteca(receitas.id, "Humor") }
        assertEquals("Doces", acervo.renomearBiblioteca(receitas.id, "Doces").nome)
    }

    @Test
    fun `Bibliotecas padrao nao podem ser renomeadas nem apagadas`() = runTest {
        val youtube = Biblioteca.idPadrao(Plataforma.YOUTUBE)

        assertFailsWith<BibliotecaPadraoImutavel> { acervo.renomearBiblioteca(youtube, "Vídeos") }
        assertFailsWith<BibliotecaPadraoImutavel> { acervo.apagarBiblioteca(youtube) }
        assertEquals("YouTube", acervo.bibliotecas().single { it.id == youtube }.nome)
    }

    @Test
    fun `mover varios Videos para outra Biblioteca nao mexe no arquivo`() = runTest {
        val a = baixar("https://youtu.be/aaaaaaa")
        val b = baixar("https://x.com/u/status/1")
        val c = baixar("https://youtu.be/ccccccc")
        val receitas = acervo.criarBiblioteca("Receitas")

        acervo.mover(listOf(a.id, b.id), receitas.id)

        assertEquals(setOf(a.id, b.id), acervo.videosDa(receitas.id).map { it.id }.toSet())
        assertEquals(listOf(c.id), acervo.videosDa(Biblioteca.idPadrao(Plataforma.YOUTUBE)).map { it.id })
        assertEquals(a.caminhoDoArquivo, acervo.video(a.id)?.caminhoDoArquivo)
        assertEquals(3, cenario.armazenamento.arquivos.size)
    }

    @Test
    fun `mover para Biblioteca inexistente e recusado`() = runTest {
        val a = baixar("https://youtu.be/aaaaaaa")

        assertFailsWith<BibliotecaNaoEncontrada> { acervo.mover(listOf(a.id), "nao-existe") }
    }

    @Test
    fun `apagar Biblioteca personalizada devolve cada Video a Biblioteca padrao da sua Plataforma`() = runTest {
        val a = baixar("https://youtu.be/aaaaaaa")
        val b = baixar("https://x.com/u/status/1")
        val receitas = acervo.criarBiblioteca("Receitas")
        acervo.mover(listOf(a.id, b.id), receitas.id)

        acervo.apagarBiblioteca(receitas.id)

        assertNull(acervo.bibliotecas().firstOrNull { it.id == receitas.id })
        assertEquals(Biblioteca.idPadrao(Plataforma.YOUTUBE), acervo.video(a.id)?.bibliotecaId)
        assertEquals(Biblioteca.idPadrao(Plataforma.X), acervo.video(b.id)?.bibliotecaId)
    }

    @Test
    fun `filtro por Plataforma de origem funciona dentro de qualquer Biblioteca`() = runTest {
        val a = baixar("https://youtu.be/aaaaaaa")
        val b = baixar("https://x.com/u/status/1")
        val receitas = acervo.criarBiblioteca("Receitas")
        acervo.mover(listOf(a.id, b.id), receitas.id)

        assertEquals(listOf(b.id), acervo.videosDa(receitas.id, plataforma = Plataforma.X).map { it.id })
        assertEquals(2, acervo.videosDa(receitas.id).size)
    }
}
