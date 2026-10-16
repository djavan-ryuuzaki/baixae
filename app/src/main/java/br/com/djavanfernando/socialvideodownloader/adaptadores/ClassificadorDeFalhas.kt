package br.com.djavanfernando.socialvideodownloader.adaptadores

import br.com.djavanfernando.socialvideodownloader.core.TipoDeFalha

/** Traduz a saída de erro do yt-dlp num [TipoDeFalha]. */
object ClassificadorDeFalhas {
    private val exigeLogin = listOf(
        "sign in to confirm",
        "login required",
        "log in",
        "private video",
        "this video is private",
        "account is private",
        "age-restricted",
        "confirm your age",
        "inappropriate for some users",
        "members-only",
        "join this channel",
        "requested content is not available",
        "cookies",
    )
    private val semRede = listOf(
        "failed to resolve",
        "temporary failure in name resolution",
        "name or service not known",
        "no address associated with hostname",
        "network is unreachable",
        "connection refused",
        "connection reset",
        "timed out",
        "read timed out",
        "remote end closed connection",
        "errno 101",
        "errno 7",
    )
    private val semEspaco = listOf("no space left on device", "errno 28")
    private val plataformaMudou = listOf(
        "unable to extract",
        "unsupported url",
        "nsig extraction failed",
        "signature extraction failed",
        "requested format is not available",
        "http error 403",
        "keyerror",
        "unable to download json",
        "unable to download webpage",
        "no video formats found",
        "there's no video in this",
    )

    fun classificar(saidaDeErro: String): TipoDeFalha {
        val texto = saidaDeErro.lowercase()
        return when {
            semEspaco.any(texto::contains) -> TipoDeFalha.SEM_ESPACO
            semRede.any(texto::contains) -> TipoDeFalha.SEM_REDE
            exigeLogin.any(texto::contains) -> TipoDeFalha.EXIGE_LOGIN
            plataformaMudou.any(texto::contains) -> TipoDeFalha.PLATAFORMA_MUDOU
            else -> TipoDeFalha.OUTRO
        }
    }
}
