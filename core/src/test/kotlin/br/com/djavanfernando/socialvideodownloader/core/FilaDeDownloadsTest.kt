package br.com.djavanfernando.socialvideodownloader.core

import br.com.djavanfernando.socialvideodownloader.core.fakes.Cenario
import br.com.djavanfernando.socialvideodownloader.core.portas.EstadoDaRede
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FilaDeDownloadsTest {
    private val cenario = Cenario()
    private val fila = cenario.fila
    private val extrator = cenario.extrator

    private fun link(n: Int) = "https://www.youtube.com/watch?v=video$n"

    private suspend fun estados() = cenario.repositorio.itensDaFila().sortedBy { it.criadoEm }.map { it.estado }

    private fun TestScope.processarEmSegundoPlano() = backgroundScope.launch { fila.processar() }

    @Test
    fun `enfileirar 5 Links mantem no maximo 2 Downloads em andamento`() = runTest {
        (1..5).forEach { n ->
            extrator.cadastrar(link(n), umPost())
            extrator.segurar(link(n))
            cenario.relogio.agora = cenario.relogio.agora.plusSeconds(1)
            fila.enfileirar(link(n))
        }

        processarEmSegundoPlano()
        runCurrent()
        assertEquals(
            listOf(EstadoNaFila.EM_ANDAMENTO, EstadoNaFila.EM_ANDAMENTO) + List(3) { EstadoNaFila.PENDENTE },
            estados(),
        )

        extrator.liberar(link(1))
        runCurrent()
        assertEquals(
            listOf(EstadoNaFila.CONCLUIDO, EstadoNaFila.EM_ANDAMENTO, EstadoNaFila.EM_ANDAMENTO) +
                List(2) { EstadoNaFila.PENDENTE },
            estados(),
        )

        (2..5).forEach { extrator.liberar(link(it)); runCurrent() }
        assertEquals(List(5) { EstadoNaFila.CONCLUIDO }, estados())
        assertEquals(5, cenario.repositorio.videos().size)
    }

    @Test
    fun `item mostra o progresso enquanto baixa e guarda os Videos gerados ao concluir`() = runTest {
        extrator.cadastrar(link(1), umPost(autor = "canal", legenda = "Titulo"))
        extrator.segurar(link(1))
        val item = fila.enfileirar(link(1))
        processarEmSegundoPlano()
        runCurrent()
        assertEquals(mapOf(item.id to 0.5f), fila.progresso.value)

        extrator.liberar(link(1))
        runCurrent()

        val concluido = cenario.repositorio.itensDaFila().single { it.id == item.id }
        assertEquals(EstadoNaFila.CONCLUIDO, concluido.estado)
        assertEquals(emptyMap(), fila.progresso.value)
        assertEquals("Titulo", concluido.titulo)
        assertEquals(cenario.repositorio.videos().map { it.id }, concluido.videoIds)
    }

    @Test
    fun `Link novo enfileirado durante um Download comeca sem esperar o outro terminar`() = runTest {
        extrator.cadastrar(link(1), umPost())
        extrator.segurar(link(1))
        fila.enfileirar(link(1))
        processarEmSegundoPlano()
        runCurrent()

        extrator.cadastrar(link(2), umPost())
        extrator.segurar(link(2))
        cenario.relogio.agora = cenario.relogio.agora.plusSeconds(1)
        fila.enfileirar(link(2))
        runCurrent()

        assertEquals(listOf(EstadoNaFila.EM_ANDAMENTO, EstadoNaFila.EM_ANDAMENTO), estados())
    }

    @Test
    fun `sem rede os Downloads aguardam e retomam quando a rede volta, sem contar como falha`() = runTest {
        cenario.conectividade.estado = EstadoDaRede.SEM_REDE
        extrator.cadastrar(link(1), umPost())
        fila.enfileirar(link(1))

        fila.processar()
        assertEquals(listOf(EstadoNaFila.AGUARDANDO_REDE), estados())
        assertTrue(fila.haDownloadsAguardandoRede())
        assertEquals(emptyList(), extrator.downloads)

        cenario.conectividade.estado = EstadoDaRede.MOVEL
        fila.processar()
        assertEquals(listOf(EstadoNaFila.CONCLUIDO), estados())
    }

    @Test
    fun `rede que cai no meio do Download faz o item aguardar rede em vez de falhar`() = runTest {
        extrator.cadastrar(link(1), umPost())
        extrator.falhar(link(1), FalhaDoExtrator(TipoDeFalha.SEM_REDE))
        fila.enfileirar(link(1))
        extrator.aoFalhar = { cenario.conectividade.estado = EstadoDaRede.SEM_REDE }

        fila.processar()

        assertEquals(listOf(EstadoNaFila.AGUARDANDO_REDE), estados())
        assertEquals(emptyMap(), cenario.armazenamento.temporarios)
    }

    @Test
    fun `erro de rede com o celular conectado e falha, sem nova tentativa automatica`() = runTest {
        extrator.cadastrar(link(1), umPost())
        extrator.falhar(link(1), FalhaDoExtrator(TipoDeFalha.SEM_REDE))
        fila.enfileirar(link(1))

        fila.processar()

        val item = cenario.repositorio.itensDaFila().single()
        assertEquals(EstadoNaFila.FALHOU, item.estado)
        assertEquals(TipoDeFalha.SEM_REDE, item.falha)
        assertEquals(false, fila.haDownloadsAguardandoRede())
    }

    @Test
    fun `Download interrompido pelo sistema volta a ficar pendente, nao cancelado`() = runTest {
        extrator.cadastrar(link(1), umPost())
        extrator.segurar(link(1))
        fila.enfileirar(link(1))
        val execucao = backgroundScope.launch { fila.processar() }
        runCurrent()

        execucao.cancel()
        runCurrent()

        assertEquals(listOf(EstadoNaFila.PENDENTE), estados())
        assertEquals(emptyMap(), cenario.armazenamento.temporarios)
    }

    @Test
    fun `com so Wi-Fi ligado os Downloads esperam ate haver Wi-Fi`() = runTest {
        cenario.preferencias.salvar(Configuracoes(soWifi = true))
        cenario.conectividade.estado = EstadoDaRede.MOVEL
        extrator.cadastrar(link(1), umPost())
        fila.enfileirar(link(1))

        fila.processar()
        assertEquals(listOf(EstadoNaFila.AGUARDANDO_REDE), estados())
        assertTrue(fila.exigeWifi())

        cenario.conectividade.estado = EstadoDaRede.WIFI
        fila.processar()
        assertEquals(listOf(EstadoNaFila.CONCLUIDO), estados())
    }

    @Test
    fun `cancelar um Download em andamento interrompe e apaga o arquivo parcial`() = runTest {
        extrator.cadastrar(link(1), umPost())
        extrator.segurar(link(1))
        val item = fila.enfileirar(link(1))
        processarEmSegundoPlano()
        runCurrent()
        assertEquals(mapOf("tmp/0.mp4" to "parcial"), cenario.armazenamento.temporarios)

        fila.cancelar(item.id)
        runCurrent()

        assertEquals(listOf(EstadoNaFila.CANCELADO), estados())
        assertEquals(emptyMap(), cenario.armazenamento.temporarios)
        assertEquals(emptyMap(), cenario.armazenamento.arquivos)
        assertEquals(emptyList(), cenario.repositorio.videos())
    }

    @Test
    fun `cancelar um Download pendente impede que ele comece`() = runTest {
        extrator.cadastrar(link(1), umPost())
        val item = fila.enfileirar(link(1))

        fila.cancelar(item.id)
        fila.processar()

        assertEquals(listOf(EstadoNaFila.CANCELADO), estados())
        assertEquals(emptyList(), extrator.downloads)
    }

    @Test
    fun `Download que falha nao e tentado de novo automaticamente`() = runTest {
        extrator.cadastrar(link(1), umPost())
        extrator.falhar(link(1), FalhaDoExtrator(TipoDeFalha.PLATAFORMA_MUDOU))
        fila.enfileirar(link(1))

        fila.processar()
        fila.processar()

        val item = cenario.repositorio.itensDaFila().single()
        assertEquals(EstadoNaFila.FALHOU, item.estado)
        assertEquals(TipoDeFalha.PLATAFORMA_MUDOU, item.falha)
        assertEquals(1, extrator.downloads.size)
    }

    @Test
    fun `erro grave do Extrator marca o Download como falho em vez de deixa-lo em andamento`() = runTest {
        extrator.cadastrar(link(1), umPost())
        extrator.falhar(link(1), ExceptionInInitializerError("biblioteca nativa quebrada"))
        fila.enfileirar(link(1))

        runCatching { fila.processar() }

        val item = cenario.repositorio.itensDaFila().single()
        assertEquals(EstadoNaFila.FALHOU, item.estado)
        assertEquals(TipoDeFalha.OUTRO, item.falha)
    }

    @Test
    fun `Downloads interrompidos pelo fim do processo voltam a ficar pendentes`() = runTest {
        extrator.cadastrar(link(1), umPost())
        val item = fila.enfileirar(link(1))
        cenario.repositorio.salvarItem(item.copy(estado = EstadoNaFila.EM_ANDAMENTO))

        fila.retomarInterrompidos()

        assertEquals(listOf(EstadoNaFila.PENDENTE), estados())
    }

    @Test
    fun `enfileirar um Link nao suportado e recusado`() = runTest {
        assertFailsWith<LinkNaoSuportado> { fila.enfileirar("https://www.tiktok.com/@a/video/1") }
        assertEquals(emptyList(), estados())
    }

    @Test
    fun `remover da fila so e permitido para itens encerrados`() = runTest {
        extrator.cadastrar(link(1), umPost())
        val pendente = fila.enfileirar(link(1))
        fila.remover(pendente.id)
        assertEquals(listOf(EstadoNaFila.PENDENTE), estados())

        fila.processar()
        fila.remover(pendente.id)
        assertEquals(emptyList(), estados())
    }
}
