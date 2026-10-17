package br.com.djavanfernando.baixae.core

import br.com.djavanfernando.baixae.core.fakes.Cenario
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LinkRepetidoTest {
    private val cenario = Cenario()

    private suspend fun baixar(link: String): Video {
        cenario.extrator.cadastrar(link, umPost())
        return cenario.baixador.baixar(link, "Movies").first()
    }

    @Test
    fun `Link ja baixado e reconhecido com a Biblioteca atual do Video`() = runTest {
        cenario.acervo.iniciar()
        val video = baixar("https://www.instagram.com/reel/abc/")

        val repetido = cenario.acervo.downloadAnterior("https://www.instagram.com/reel/abc/")

        assertEquals(video, repetido?.video)
        assertEquals("Instagram", repetido?.biblioteca?.nome)
    }

    @Test
    fun `Link nunca baixado nao e repetido`() = runTest {
        cenario.acervo.iniciar()
        baixar("https://www.instagram.com/reel/abc/")

        assertNull(cenario.acervo.downloadAnterior("https://www.instagram.com/reel/outro/"))
    }

    @Test
    fun `variacoes do mesmo Link contam como repetido`() = runTest {
        cenario.acervo.iniciar()
        baixar("https://www.instagram.com/reel/abc/?igsh=xyz")
        baixar("https://youtu.be/VIDEO123?si=rastreio")

        listOf(
            "https://instagram.com/reel/abc",
            "https://www.instagram.com/reel/abc/?utm_source=ig_web_copy_link",
            "https://www.youtube.com/watch?v=VIDEO123&feature=share",
            "https://m.youtube.com/shorts/VIDEO123",
        ).forEach { assertEquals(true, cenario.acervo.downloadAnterior(it) != null, it) }
        assertNull(cenario.acervo.downloadAnterior("https://www.youtube.com/watch?v=OUTRO"))
    }

    @Test
    fun `sugestao da area de transferencia so para Links suportados`() {
        assertEquals("https://x.com/a/status/1", Plataforma.sugestaoDeDownload("olha https://x.com/a/status/1"))
        assertNull(Plataforma.sugestaoDeDownload("https://www.tiktok.com/@a/video/1"))
        assertNull(Plataforma.sugestaoDeDownload("lista de compras"))
        assertNull(Plataforma.sugestaoDeDownload(null))
    }
}
