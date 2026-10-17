package br.com.djavanfernando.baixae.core

/**
 * Forma canônica de um Link, para reconhecer o mesmo Post copiado de jeitos diferentes
 * (com ou sem `www.`, barra final, parâmetros de rastreio, `youtu.be` × `youtube.com`).
 */
object ChaveDoLink {
    private val parametrosDeRastreio = setOf("igsh", "igshid", "si", "feature", "s", "ref", "ref_src", "mibextid", "fbclid")
    private val partes = Regex("""^https?://([^/?#]+)([^?#]*)(?:\?([^#]*))?""", RegexOption.IGNORE_CASE)
    private val idDoYoutube = Regex("""^/(?:shorts/|live/|embed/)?([A-Za-z0-9_-]{6,})""")

    fun de(link: String): String {
        val encontrado = partes.find(link.trim()) ?: return link.trim()
        val host = encontrado.groupValues[1].lowercase().removePrefix("www.").removePrefix("m.").removePrefix("mobile.")
        val caminho = encontrado.groupValues[2].trimEnd('/')
        val parametros = encontrado.groupValues[3].split('&')
            .filter { it.isNotEmpty() }
            .map { it.substringBefore('=') to it.substringAfter('=', "") }
        youtube(host, caminho, parametros)?.let { return "youtube:$it" }
        val mantidos = parametros
            .filter { (nome, _) -> nome.lowercase() !in parametrosDeRastreio && !nome.lowercase().startsWith("utm_") }
            .sortedBy { it.first }
            .joinToString("&") { "${it.first}=${it.second}" }
        return host + caminho + if (mantidos.isEmpty()) "" else "?$mantidos"
    }

    private fun youtube(host: String, caminho: String, parametros: List<Pair<String, String>>): String? = when {
        host == "youtu.be" -> idDoYoutube.find(caminho)?.groupValues?.get(1)
        host == "youtube.com" && caminho == "/watch" -> parametros.firstOrNull { it.first == "v" }?.second
        host == "youtube.com" -> idDoYoutube.find(caminho)?.groupValues?.get(1)
            ?.takeIf { caminho.startsWith("/shorts/") || caminho.startsWith("/live/") || caminho.startsWith("/embed/") }
        else -> null
    }
}
