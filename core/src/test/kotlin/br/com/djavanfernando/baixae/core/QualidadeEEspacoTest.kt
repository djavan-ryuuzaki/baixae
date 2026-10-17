package br.com.djavanfernando.baixae.core

import br.com.djavanfernando.baixae.core.fakes.Cenario
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class QualidadeEEspacoTest {
    private val cenario = Cenario()
    private val link = "https://youtu.be/aaaaaaa"

    private suspend fun baixarPelaFila(): ItemDaFila {
        val item = cenario.fila.enfileirar(link)
        cenario.fila.processar()
        return cenario.repositorio.itemDaFila(item.id)!!
    }

    @Test
    fun `limite de qualidade padrao e 1080p`() {
        assertEquals(LimiteDeQualidade.P1080, Configuracoes().limiteDeQualidade)
    }

    @Test
    fun `limite de qualidade das Configuracoes e repassado ao Extrator`() = runTest {
        cenario.preferencias.salvar(Configuracoes(limiteDeQualidade = LimiteDeQualidade.P720))
        cenario.extrator.cadastrar(link, umPost(videos = 2))

        baixarPelaFila()

        assertEquals(listOf(LimiteDeQualidade.P720, LimiteDeQualidade.P720), cenario.extrator.limitesPedidos)
    }

    @Test
    fun `sem espaco suficiente o Download falha antes de comecar`() = runTest {
        cenario.extrator.cadastrar(link, umPost(videos = 2, tamanhoDeCadaVideo = 400))
        cenario.armazenamento.espacoLivre = 1_000

        val item = baixarPelaFila()

        assertEquals(EstadoNaFila.FALHOU, item.estado)
        assertEquals(TipoDeFalha.SEM_ESPACO, item.falha)
        assertEquals(emptyList(), cenario.extrator.downloads)
    }

    @Test
    fun `com espaco suficiente o Download acontece`() = runTest {
        cenario.extrator.cadastrar(link, umPost(videos = 2, tamanhoDeCadaVideo = 400))
        cenario.armazenamento.espacoLivre = 2_000

        assertEquals(EstadoNaFila.CONCLUIDO, baixarPelaFila().estado)
    }

    @Test
    fun `tamanho desconhecido nao impede o Download`() = runTest {
        cenario.extrator.cadastrar(link, umPost(tamanhoDeCadaVideo = null))
        cenario.armazenamento.espacoLivre = 0

        assertEquals(EstadoNaFila.CONCLUIDO, baixarPelaFila().estado)
    }

    @Test
    fun `videos longos nao tem limite de duracao`() = runTest {
        cenario.extrator.cadastrar(link, umPost().copy(duracaoEmSegundos = 10 * 3600))

        assertEquals(EstadoNaFila.CONCLUIDO, baixarPelaFila().estado)
        assertEquals(10L * 3600, cenario.repositorio.videos().single().duracaoEmSegundos)
    }
}
