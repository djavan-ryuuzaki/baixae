package br.com.djavanfernando.baixae.core

import br.com.djavanfernando.baixae.core.fakes.Cenario
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BaixadorTest {
    private val cenario = Cenario()
    private val armazenamento = cenario.armazenamento
    private val extrator = cenario.extrator
    private val repositorio = cenario.repositorio
    private val baixador = cenario.baixador

    @Test
    fun `Link do YouTube gera um Video registrado com o arquivo salvo na Pasta de destino`() = runTest {
        val link = "https://www.youtube.com/watch?v=abc123"
        extrator.cadastrar(link, umPost(autor = "Canal Exemplo", videos = 1))

        val videos = baixador.baixar(link, pasta = "Movies/Baixaê")

        val video = videos.single()
        assertEquals(listOf(video), repositorio.videos())
        assertEquals(Plataforma.YOUTUBE, video.plataforma)
        assertEquals(link, video.link)
        assertEquals("conteudo de $link #0", armazenamento.arquivos[video.caminhoDoArquivo])
        assertEquals("Movies/Baixaê/${video.nome}.mp4", video.caminhoDoArquivo)
        assertEquals(emptyMap(), armazenamento.temporarios)
    }

    @Test
    fun `Link de fora das Plataformas e recusado sem acionar o Extrator`() = runTest {
        assertFailsWith<LinkNaoSuportado> { baixador.baixar("https://www.tiktok.com/@a/video/1", "Movies") }
        assertEquals(emptyList(), extrator.downloads)
        assertEquals(emptyList(), repositorio.videos())
    }

    @Test
    fun `Video guarda a Plataforma reconhecida no Link`() = runTest {
        val link = "https://twitter.com/user/status/1"
        extrator.cadastrar(link, umPost())

        val video = baixador.baixar(link, "Movies").single()

        assertEquals(Plataforma.X, video.plataforma)
    }
}

fun umPost(
    autor: String = "autor",
    legenda: String = "legenda",
    videos: Int = 1,
    fotos: Int = 0,
    tamanhoDeCadaVideo: Long? = 1_000,
) = Post(
    autor = autor,
    legenda = legenda,
    duracaoEmSegundos = 30,
    miniatura = "https://exemplo/miniatura.jpg",
    midias = List(videos) { MidiaDoPost(indice = it, tipo = TipoDeMidia.VIDEO, tamanhoEstimadoEmBytes = tamanhoDeCadaVideo) } +
        List(fotos) { MidiaDoPost(indice = videos + it, tipo = TipoDeMidia.FOTO, tamanhoEstimadoEmBytes = 10) },
)
