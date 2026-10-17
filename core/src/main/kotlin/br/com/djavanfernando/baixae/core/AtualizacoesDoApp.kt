package br.com.djavanfernando.baixae.core

import br.com.djavanfernando.baixae.core.portas.Preferencias
import br.com.djavanfernando.baixae.core.portas.VerificadorDeVersoes
import br.com.djavanfernando.baixae.core.portas.VersaoPublicada

/** Comparação de versões no formato `1.2.3` (com ou sem `v`). Sufixos como `-beta` contam como anteriores. */
object Versao {
    fun maisNova(candidata: String, atual: String): Boolean = comparar(candidata, atual) > 0

    private fun comparar(a: String, b: String): Int {
        val (numerosA, preA) = partes(a)
        val (numerosB, preB) = partes(b)
        for (i in 0 until maxOf(numerosA.size, numerosB.size)) {
            val diferenca = numerosA.getOrElse(i) { 0 }.compareTo(numerosB.getOrElse(i) { 0 })
            if (diferenca != 0) return diferenca
        }
        return when {
            preA == preB -> 0
            preA -> -1
            else -> 1
        }
    }

    private fun partes(versao: String): Pair<List<Int>, Boolean> {
        val limpa = versao.trim().removePrefix("v").removePrefix("V")
        val principal = limpa.substringBefore('-')
        return principal.split('.').map { it.toIntOrNull() ?: 0 } to (limpa != principal)
    }
}

/** Aviso de nova versão do app publicada no GitHub Releases. A instalação fica com o usuário. */
class AtualizacoesDoApp(
    private val verificador: VerificadorDeVersoes,
    private val preferencias: Preferencias,
    private val versaoInstalada: String,
) {
    /** Consulta a última release; devolve a versão nova, ou `null` se não houver (ou sem rede). */
    suspend fun verificar(): VersaoPublicada? {
        val publicada = try {
            verificador.ultimaVersao()
        } catch (_: Exception) {
            return null
        } ?: return null
        if (!Versao.maisNova(publicada.versao, versaoInstalada)) return null
        preferencias.salvar(preferencias.ler().copy(novaVersao = publicada))
        return publicada
    }

    /** Nova versão já encontrada e ainda mais nova que a instalada. */
    suspend fun novaVersaoDisponivel(): VersaoPublicada? =
        preferencias.ler().novaVersao?.takeIf { Versao.maisNova(it.versao, versaoInstalada) }
}

/** A tarefa periódica semanal: Extratores e nova versão do app. */
class VerificacaoSemanal(
    private val extratores: Extratores,
    private val atualizacoes: AtualizacoesDoApp,
) {
    suspend fun executar() {
        try {
            extratores.verificacaoSemanal()
        } finally {
            atualizacoes.verificar()
        }
    }
}
