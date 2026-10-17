package br.com.djavanfernando.baixae.core

import br.com.djavanfernando.baixae.core.fakes.Cenario
import kotlinx.coroutines.test.runTest
import java.time.Instant
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals

class NomesTest {
    private val cenario = Cenario()

    @Test
    fun `nome padrao e plataforma, autor e data do Download`() = runTest {
        val link = "https://x.com/a/status/1"
        cenario.extrator.cadastrar(link, umPost(autor = "Maria"))

        val video = cenario.baixador.baixar(link, "Movies/SVD").single()

        assertEquals("x_Maria_2026-10-16", video.nome)
        assertEquals("Movies/SVD/x_Maria_2026-10-16.mp4", video.caminhoDoArquivo)
    }

    @Test
    fun `data do nome segue o fuso do celular`() = runTest {
        cenario.relogio.agora = Instant.parse("2026-10-17T01:30:00Z")
        cenario.relogio.fuso = ZoneId.of("America/Sao_Paulo")
        val link = "https://x.com/a/status/1"
        cenario.extrator.cadastrar(link, umPost(autor = "Maria"))

        assertEquals("x_Maria_2026-10-16", cenario.baixador.baixar(link, "Movies").single().nome)
    }

    @Test
    fun `caracteres invalidos em nomes de arquivo sao sanitizados`() {
        assertEquals("instagram_a_b_c_d_2026-10-16", NomeDeVideo.padrao(Plataforma.INSTAGRAM, "a/b:c*d", java.time.LocalDate.parse("2026-10-16")))
        assertEquals("youtube_Canal Legal_2026-10-16", NomeDeVideo.padrao(Plataforma.YOUTUBE, "  Canal\tLegal?  ", java.time.LocalDate.parse("2026-10-16")))
        assertEquals("bluesky_desconhecido_2026-10-16", NomeDeVideo.padrao(Plataforma.BLUESKY, "///", java.time.LocalDate.parse("2026-10-16")))
    }

    @Test
    fun `nomes repetidos na mesma pasta recebem sufixos _2 e _3`() = runTest {
        val link = "https://x.com/a/status/1"
        cenario.extrator.cadastrar(link, umPost(autor = "Maria"))

        val nomes = (1..3).map { cenario.baixador.baixar(link, "Movies").single().nome }

        assertEquals(listOf("x_Maria_2026-10-16", "x_Maria_2026-10-16_2", "x_Maria_2026-10-16_3"), nomes)
    }

    @Test
    fun `o mesmo nome em pastas diferentes nao recebe sufixo`() = runTest {
        val link = "https://x.com/a/status/1"
        cenario.extrator.cadastrar(link, umPost(autor = "Maria"))

        val primeiro = cenario.baixador.baixar(link, "Movies/A").single()
        val segundo = cenario.baixador.baixar(link, "Movies/B").single()

        assertEquals(primeiro.nome, segundo.nome)
    }

    @Test
    fun `se o armazenamento salvar com outro nome, o Video fica com o nome real do arquivo`() = runTest {
        val link = "https://x.com/a/status/1"
        cenario.extrator.cadastrar(link, umPost(autor = "Maria"))
        cenario.armazenamento.ocultos += "Movies/x_Maria_2026-10-16.mp4"

        val video = cenario.baixador.baixar(link, "Movies").single()

        assertEquals("x_Maria_2026-10-16 (1)", video.nome)
        assertEquals("Movies/x_Maria_2026-10-16 (1).mp4", video.caminhoDoArquivo)
    }
}
