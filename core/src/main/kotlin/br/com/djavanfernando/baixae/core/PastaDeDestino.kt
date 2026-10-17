package br.com.djavanfernando.baixae.core

/**
 * Pasta de destino: caminho relativo à raiz do armazenamento compartilhado. Fica restrita às pastas
 * em que o Android aceita vídeos de apps sem permissão especial.
 */
object PastaDeDestino {
    val raizesPermitidas = listOf("Movies", "DCIM", "Pictures")

    /** Caminho normalizado, ou `null` se não for uma Pasta de destino aceita. */
    fun validar(texto: String): String? {
        val partes = texto.split('/', '\\').map { it.trim() }.filter { it.isNotEmpty() }
        if (partes.isEmpty() || partes.any { it == "." || it == ".." }) return null
        val raiz = raizesPermitidas.firstOrNull { it.equals(partes.first(), ignoreCase = true) } ?: return null
        val subpastas = partes.drop(1).map(NomeDeVideo::sanitizar).filter { it.isNotEmpty() }
        return (listOf(raiz) + subpastas).joinToString("/")
    }
}
