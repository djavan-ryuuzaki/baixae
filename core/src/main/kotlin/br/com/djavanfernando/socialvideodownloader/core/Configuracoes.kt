package br.com.djavanfernando.socialvideodownloader.core

/** Preferências do usuário, editadas na aba Configurações. */
data class Configuracoes(
    val pastaPadrao: String = PASTA_PADRAO,
    val soWifi: Boolean = false,
    val limiteDeQualidade: LimiteDeQualidade = LimiteDeQualidade.P1080,
    val marcaDagua: Boolean = false,
) {
    companion object {
        const val PASTA_PADRAO = "Movies/SocialVideoDownloader"
    }
}
