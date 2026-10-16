package br.com.djavanfernando.socialvideodownloader.core

import br.com.djavanfernando.socialvideodownloader.core.fakes.Cenario
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BuscaEOrdenacaoTest {
    private val cenario = Cenario()
    private val acervo = cenario.acervo
    private val receitas = "receitas"

    private suspend fun baixar(link: String, autor: String, legenda: String, horas: Long): Video {
        cenario.relogio.agora = java.time.Instant.parse("2026-10-16T00:00:00Z").plusSeconds(horas * 3600)
        cenario.extrator.cadastrar(link, umPost(autor = autor, legenda = legenda))
        val video = cenario.baixador.baixar(link, "Movies").single()
        acervo.mover(listOf(video.id), receitas)
        return acervo.video(video.id)!!
    }

    private suspend fun preparar(): List<Video> {
        cenario.repositorio.salvarBiblioteca(Biblioteca(receitas, "Receitas"))
        return listOf(
            baixar("https://youtu.be/aaaaaaa", "Ana Maria", "Pão de queijo", horas = 1),
            baixar("https://x.com/u/status/1", "chef", "Feijoada completa", horas = 3),
            baixar("https://youtu.be/ccccccc", "Érico", "Bolo de CENOURA", horas = 2),
        )
    }

    @Test
    fun `Videos aparecem do Download mais recente para o mais antigo`() = runTest {
        val (pao, feijoada, bolo) = preparar()

        assertEquals(listOf(feijoada.id, bolo.id, pao.id), acervo.videosDa(receitas).map { it.id })
    }

    @Test
    fun `busca por legenda sem diferenciar maiusculas nem acentos`() = runTest {
        val (pao, _, bolo) = preparar()

        assertEquals(listOf(bolo.id), acervo.videosDa(receitas, busca = "cenoura").map { it.id })
        assertEquals(listOf(pao.id), acervo.videosDa(receitas, busca = "PAO").map { it.id })
    }

    @Test
    fun `busca por autor e por nome`() = runTest {
        val (pao, feijoada, bolo) = preparar()

        assertEquals(listOf(bolo.id), acervo.videosDa(receitas, busca = "erico").map { it.id })
        assertEquals(listOf(feijoada.id), acervo.videosDa(receitas, busca = "x_chef").map { it.id })
        assertEquals(listOf(feijoada.id, bolo.id, pao.id), acervo.videosDa(receitas, busca = "  ").map { it.id })
    }

    @Test
    fun `busca combinada com o filtro por Plataforma`() = runTest {
        val (pao, _, bolo) = preparar()

        assertEquals(listOf(bolo.id, pao.id), acervo.videosDa(receitas, plataforma = Plataforma.YOUTUBE, busca = "DE").map { it.id })
        assertEquals(emptyList(), acervo.videosDa(receitas, plataforma = Plataforma.X, busca = "bolo"))
    }
}
