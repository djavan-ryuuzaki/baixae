package br.com.djavanfernando.socialvideodownloader.core

import br.com.djavanfernando.socialvideodownloader.core.fakes.Cenario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class CarrosselTest {
    private val cenario = Cenario()
    private val link = "https://www.instagram.com/p/carrossel/"

    @Test
    fun `Post com 3 videos e 2 fotos gera 3 Videos na Biblioteca padrao, com sufixos e o mesmo Link`() = runTest {
        cenario.acervo.iniciar()
        cenario.extrator.cadastrar(link, umPost(autor = "chef", videos = 3, fotos = 2))

        val item = cenario.fila.enfileirar(link)
        cenario.fila.processar()

        val videos = cenario.repositorio.videos().sortedBy { it.nome }
        assertEquals(
            listOf("instagram_chef_2026-10-16", "instagram_chef_2026-10-16_2", "instagram_chef_2026-10-16_3"),
            videos.map { it.nome },
        )
        assertEquals(listOf(0, 1, 2), cenario.extrator.downloads.map { it.second })
        assertEquals(setOf(link), videos.map { it.link }.toSet())
        assertEquals(setOf(Biblioteca.idPadrao(Plataforma.INSTAGRAM)), videos.map { it.bibliotecaId }.toSet())
        val concluido = cenario.repositorio.itensDaFila().single()
        assertEquals(item.id, concluido.id)
        assertEquals(videos.map { it.id }.toSet(), concluido.videoIds.toSet())
    }

    @Test
    fun `progresso do carrossel e o do Post inteiro, num unico item da fila`() = runTest {
        cenario.extrator.cadastrar(link, umPost(videos = 2))
        cenario.extrator.segurar(link)
        val item = cenario.fila.enfileirar(link)

        backgroundScope.launch { cenario.fila.processar() }
        runCurrent()

        assertEquals(1, cenario.repositorio.itensDaFila().size)
        assertEquals(mapOf(item.id to 0.25f), cenario.fila.progresso.value)
    }
}
