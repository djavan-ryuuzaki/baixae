package br.com.djavanfernando.socialvideodownloader.adaptadores

import br.com.djavanfernando.socialvideodownloader.core.TipoDeFalha

/** Traduz a saída de erro do yt-dlp num [TipoDeFalha]. */
object ClassificadorDeFalhas {
    /** Bloqueios temporários (anti-robô, limite de requisições): o yt-dlp sugere cookies, mas não é Post privado. */
    private val bloqueioTemporario = listOf("not a bot", "too many requests", "http error 429")
    private val exigeLogin = listOf(
        "sign in to confirm your age",
        "login required",
        "private video",
        "this video is private",
        "account is private",
        "age-restricted",
        "confirm your age",
        "inappropriate for some users",
        "members-only",
        "join this channel",
        "requested content is not available",
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
            bloqueioTemporario.any(texto::contains) -> TipoDeFalha.OUTRO
            exigeLogin.any(texto::contains) -> TipoDeFalha.EXIGE_LOGIN
            plataformaMudou.any(texto::contains) -> TipoDeFalha.PLATAFORMA_MUDOU
            else -> TipoDeFalha.OUTRO
        }
    }
}
