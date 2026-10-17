package br.com.djavanfernando.baixae.core.portas

/** Uma versão do app publicada, com o endereço da página da release (onde está o APK). */
data class VersaoPublicada(val versao: String, val endereco: String)

/** Consulta a versão mais nova do app publicada no GitHub Releases. */
interface VerificadorDeVersoes {
    suspend fun ultimaVersao(): VersaoPublicada?
}
