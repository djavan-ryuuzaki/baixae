package br.com.djavanfernando.baixae.core

/**
 * Agrupamento de Vídeos que existe só dentro do app.
 * Com [plataforma], é a Biblioteca padrão daquela Plataforma; sem, é uma Biblioteca personalizada.
 */
data class Biblioteca(
    val id: String,
    val nome: String,
    val plataforma: Plataforma? = null,
) {
    val padrao: Boolean get() = plataforma != null

    companion object {
        fun idPadrao(plataforma: Plataforma) = "padrao-${plataforma.name}"

        fun padrao(plataforma: Plataforma) = Biblioteca(idPadrao(plataforma), plataforma.nome, plataforma)
    }
}
