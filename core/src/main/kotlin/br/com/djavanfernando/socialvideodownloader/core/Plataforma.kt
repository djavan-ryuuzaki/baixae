package br.com.djavanfernando.socialvideodownloader.core

/** Serviço social suportado, de onde Vídeos são baixados. */
enum class Plataforma(val nome: String, val instavel: Boolean = false) {
    INSTAGRAM("Instagram"),
    FACEBOOK("Facebook", instavel = true),
    X("X"),
    BLUESKY("Bluesky"),
    YOUTUBE("YouTube");

    companion object {
        /** Reconhece a Plataforma pelo domínio do Link; `null` para Links de fora das Plataformas. */
        fun reconhecer(link: String): Plataforma? {
            val host = Regex("^https?://([^/?#]+)", RegexOption.IGNORE_CASE)
                .find(link.trim())?.groupValues?.get(1)?.lowercase() ?: return null
            return when {
                host == "youtube.com" || host.endsWith(".youtube.com") || host == "youtu.be" -> YOUTUBE
                else -> null
            }
        }
    }
}
