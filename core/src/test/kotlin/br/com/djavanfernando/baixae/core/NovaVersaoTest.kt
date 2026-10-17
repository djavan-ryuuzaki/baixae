package br.com.djavanfernando.baixae.core

import br.com.djavanfernando.baixae.core.fakes.Cenario
import br.com.djavanfernando.baixae.core.portas.VersaoPublicada
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NovaVersaoTest {
    private val cenario = Cenario()
    private val atualizacoes = cenario.atualizacoesDoApp
    private val release = "https://github.com/djavan-ryuuzaki/baixae/releases/tag/v0.2.0"

    @Test
    fun `compara versoes numericamente, ignorando o prefixo v`() {
        assertTrue(Versao.maisNova("v0.2.0", "0.1.9"))
        assertTrue(Versao.maisNova("0.10.0", "0.9.5"))
        assertTrue(Versao.maisNova("1.0", "0.9.9"))
        assertTrue(Versao.maisNova("0.1.1", "0.1"))
        assertEquals(false, Versao.maisNova("0.1.0", "0.1.0"))
        assertEquals(false, Versao.maisNova("v0.1.0", "0.2.0"))
        assertEquals(false, Versao.maisNova("0.2.0-beta", "0.2.0"))
    }

    @Test
    fun `havendo versao nova, ela fica registrada com o link da release`() = runTest {
        cenario.verificadorDeVersoes.publicada = VersaoPublicada("v0.2.0", release)

        val nova = atualizacoes.verificar()

        assertEquals(VersaoPublicada("v0.2.0", release), nova)
        assertEquals(nova, atualizacoes.novaVersaoDisponivel())
    }

    @Test
    fun `sem versao nova nada e exibido`() = runTest {
        cenario.verificadorDeVersoes.publicada = VersaoPublicada("v0.1.0", release)

        assertNull(atualizacoes.verificar())
        assertNull(atualizacoes.novaVersaoDisponivel())
    }

    @Test
    fun `sem rede nada e exibido`() = runTest {
        cenario.verificadorDeVersoes.falhar = true

        assertNull(atualizacoes.verificar())
        assertNull(atualizacoes.novaVersaoDisponivel())
    }

    @Test
    fun `aviso some depois que o app e atualizado`() = runTest {
        cenario.verificadorDeVersoes.publicada = VersaoPublicada("v0.2.0", release)
        atualizacoes.verificar()

        val depoisDeAtualizar = AtualizacoesDoApp(cenario.verificadorDeVersoes, cenario.preferencias, versaoInstalada = "0.2.0")

        assertNull(depoisDeAtualizar.novaVersaoDisponivel())
    }

    @Test
    fun `verificacao semanal confere Extratores e nova versao do app na mesma tarefa`() = runTest {
        cenario.verificadorDeVersoes.publicada = VersaoPublicada("v0.2.0", release)

        cenario.verificacaoSemanal.executar()

        assertEquals(1, cenario.extrator.atualizacoes)
        assertEquals("v0.2.0", atualizacoes.novaVersaoDisponivel()?.versao)
    }
}
