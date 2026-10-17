package br.com.djavanfernando.baixae.core

import br.com.djavanfernando.baixae.core.portas.VersaoPublicada
import java.time.Instant

/** Preferências do usuário, editadas na aba Configurações. */
data class Configuracoes(
    val pastaPadrao: String = PASTA_PADRAO,
    val soWifi: Boolean = false,
    val limiteDeQualidade: LimiteDeQualidade = LimiteDeQualidade.P1080,
    val marcaDagua: Boolean = false,
    val ultimaVerificacaoDeExtratores: Instant? = null,
    /** Última versão nova do app encontrada no GitHub Releases. */
    val novaVersao: VersaoPublicada? = null,
) {
    companion object {
        const val PASTA_PADRAO = "Movies/Baixaê"
    }
}
