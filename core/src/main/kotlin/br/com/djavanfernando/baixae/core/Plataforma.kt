package br.com.djavanfernando.baixae.core

/** Serviço social suportado, de onde Vídeos são baixados. */
enum class Plataforma(
    val nome: String,
    private val dominios: Set<String>,
    val instavel: Boolean = false,
) {
    INSTAGRAM("Instagram", setOf("instagram.com", "instagr.am")),
    FACEBOOK("Facebook", setOf("facebook.com", "fb.watch", "fb.com"), instavel = true),
    X("X", setOf("x.com", "twitter.com")),
    BLUESKY("Bluesky", setOf("bsky.app")),
    YOUTUBE("YouTube", setOf("youtube.com", "youtu.be"));

    private fun reconhece(host: String) = dominios.any { host == it || host.endsWith(".$it") }

    companion object {
        private val padraoDeLink = Regex("""https?://[^\s<>"']+""", RegexOption.IGNORE_CASE)
        private val padraoDeHost = Regex("""^https?://(?:[^@/?#]*@)?([^/?#:]+)""", RegexOption.IGNORE_CASE)

        /** Reconhece a Plataforma pelo domínio do Link; `null` para Links de fora das Plataformas. */
        fun reconhecer(link: String): Plataforma? {
            val host = padraoDeHost.find(link.trim())?.groupValues?.get(1)?.lowercase() ?: return null
            return entries.firstOrNull { it.reconhece(host) }
        }

        /** Primeiro Link (http/https) contido num texto, como o enviado pelo menu Compartilhar. */
        fun extrairLink(texto: String): String? = padraoDeLink.find(texto)?.value?.trimEnd('.', ',', ')', '!', '?')

        /** Link a sugerir para Download a partir do texto da área de transferência, se for de uma Plataforma. */
        fun sugestaoDeDownload(texto: String?): String? =
            texto?.let(::extrairLink)?.takeIf { reconhecer(it) != null }
    }
}

/** Link de fora das Plataformas suportadas. */
class LinkNaoSuportado(val link: String) : IllegalArgumentException("Link não suportado: $link")
