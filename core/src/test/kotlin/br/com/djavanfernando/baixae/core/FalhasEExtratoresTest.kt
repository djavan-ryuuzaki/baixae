package br.com.djavanfernando.baixae.core

import br.com.djavanfernando.baixae.core.fakes.Cenario
import kotlinx.coroutines.test.runTest
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals

class FalhasEExtratoresTest {
    private val cenario = Cenario()
    private val link = "https://www.instagram.com/reel/abc/"

    private suspend fun falharCom(tipo: TipoDeFalha): ItemDaFila {
        cenario.extrator.cadastrar(link, umPost())
        cenario.extrator.falhar(link, FalhaDoExtrator(tipo))
        val item = cenario.fila.enfileirar(link)
        cenario.fila.processar()
        return cenario.repositorio.itemDaFila(item.id)!!
    }

    @Test
    fun `cada tipo de falha leva a uma acao`() {
        assertEquals(AcaoAposFalha.ATUALIZAR_EXTRATORES_E_TENTAR, AcaoAposFalha.para(TipoDeFalha.PLATAFORMA_MUDOU))
        assertEquals(AcaoAposFalha.NENHUMA, AcaoAposFalha.para(TipoDeFalha.EXIGE_LOGIN))
        assertEquals(AcaoAposFalha.TENTAR_DE_NOVO, AcaoAposFalha.para(TipoDeFalha.SEM_ESPACO))
        assertEquals(AcaoAposFalha.TENTAR_DE_NOVO, AcaoAposFalha.para(TipoDeFalha.OUTRO))
    }

    @Test
    fun `falha por Plataforma que mudou guarda o tipo e a Plataforma`() = runTest {
        val item = falharCom(TipoDeFalha.PLATAFORMA_MUDOU)

        assertEquals(EstadoNaFila.FALHOU, item.estado)
        assertEquals(TipoDeFalha.PLATAFORMA_MUDOU, item.falha)
        assertEquals(Plataforma.INSTAGRAM, item.plataforma)
    }

    @Test
    fun `atualizar extratores e tentar de novo atualiza o yt-dlp e reenfileira o Download`() = runTest {
        val item = falharCom(TipoDeFalha.PLATAFORMA_MUDOU)

        cenario.extratores.atualizarETentarDeNovo(item.id)
        assertEquals(1, cenario.extrator.atualizacoes)
        assertEquals(EstadoNaFila.PENDENTE, cenario.repositorio.itemDaFila(item.id)?.estado)
        assertEquals(null, cenario.repositorio.itemDaFila(item.id)?.falha)

        cenario.fila.processar()
        assertEquals(EstadoNaFila.CONCLUIDO, cenario.repositorio.itemDaFila(item.id)?.estado)
    }

    @Test
    fun `Post que exige login nao pode ser tentado de novo`() = runTest {
        val item = falharCom(TipoDeFalha.EXIGE_LOGIN)

        cenario.fila.tentarDeNovo(item.id)

        assertEquals(EstadoNaFila.FALHOU, cenario.repositorio.itemDaFila(item.id)?.estado)
    }

    @Test
    fun `tentar de novo so vale para itens que falharam`() = runTest {
        cenario.extrator.cadastrar(link, umPost())
        val item = cenario.fila.enfileirar(link)
        cenario.fila.processar()

        cenario.fila.tentarDeNovo(item.id)

        assertEquals(EstadoNaFila.CONCLUIDO, cenario.repositorio.itemDaFila(item.id)?.estado)
    }

    @Test
    fun `versao atual e atualizacao manual dos Extratores`() = runTest {
        assertEquals("2026.01.01", cenario.extratores.versao())

        assertEquals("2026.10.01", cenario.extratores.atualizar())
        assertEquals("2026.10.01", cenario.extratores.versao())
    }

    @Test
    fun `verificacao semanal atualiza no maximo uma vez por semana`() = runTest {
        assertEquals(true, cenario.extratores.verificacaoSemanal())
        assertEquals(1, cenario.extrator.atualizacoes)

        cenario.relogio.agora = cenario.relogio.agora.plus(Duration.ofDays(3))
        assertEquals(false, cenario.extratores.verificacaoSemanal())
        assertEquals(1, cenario.extrator.atualizacoes)

        cenario.relogio.agora = cenario.relogio.agora.plus(Duration.ofDays(5))
        assertEquals(true, cenario.extratores.verificacaoSemanal())
        assertEquals(2, cenario.extrator.atualizacoes)
    }
}
