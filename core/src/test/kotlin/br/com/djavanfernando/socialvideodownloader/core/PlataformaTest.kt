package br.com.djavanfernando.socialvideodownloader.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlataformaTest {
    @Test
    fun `reconhece cada Plataforma pelo dominio do Link`() {
        val casos = mapOf(
            "https://www.instagram.com/reel/C1a2b3/" to Plataforma.INSTAGRAM,
            "https://instagram.com/p/C1a2b3/?img_index=1" to Plataforma.INSTAGRAM,
            "https://www.facebook.com/watch/?v=123" to Plataforma.FACEBOOK,
            "https://m.facebook.com/story.php?story_fbid=1" to Plataforma.FACEBOOK,
            "https://fb.watch/abcDEF/" to Plataforma.FACEBOOK,
            "https://twitter.com/user/status/1" to Plataforma.X,
            "https://mobile.twitter.com/user/status/1" to Plataforma.X,
            "https://x.com/user/status/1" to Plataforma.X,
            "https://bsky.app/profile/a.bsky.social/post/3k" to Plataforma.BLUESKY,
            "https://www.youtube.com/watch?v=abc" to Plataforma.YOUTUBE,
            "https://m.youtube.com/watch?v=abc" to Plataforma.YOUTUBE,
            "https://youtube.com/shorts/abc" to Plataforma.YOUTUBE,
            "https://youtu.be/abc" to Plataforma.YOUTUBE,
            "HTTPS://WWW.YOUTUBE.COM/watch?v=abc" to Plataforma.YOUTUBE,
        )
        casos.forEach { (link, esperada) -> assertEquals(esperada, Plataforma.reconhecer(link), link) }
    }

    @Test
    fun `recusa Links de fora das Plataformas`() {
        listOf(
            "https://www.tiktok.com/@a/video/1",
            "https://www.threads.net/@a/post/1",
            "https://vimeo.com/1",
            "https://notyoutube.com/watch?v=abc",
            "https://youtube.com.golpe.io/watch",
            "youtube.com/watch?v=abc sem esquema",
            "texto qualquer",
        ).forEach { assertNull(Plataforma.reconhecer(it), it) }
    }

    @Test
    fun `Facebook e a unica Plataforma instavel`() {
        assertEquals(listOf(Plataforma.FACEBOOK), Plataforma.entries.filter { it.instavel })
    }

    @Test
    fun `extrai o Link de um texto compartilhado`() {
        assertEquals(
            "https://www.instagram.com/reel/C1a2b3/?igsh=xyz",
            Plataforma.extrairLink("Olha esse reel https://www.instagram.com/reel/C1a2b3/?igsh=xyz muito bom"),
        )
        assertEquals("https://x.com/a/status/1", Plataforma.extrairLink("https://x.com/a/status/1"))
        assertNull(Plataforma.extrairLink("sem link aqui"))
    }

    @Test
    fun `lista de Plataformas suportadas nao inclui Threads`() {
        assertTrue(Plataforma.entries.none { it.nome.contains("Threads") })
    }
}
