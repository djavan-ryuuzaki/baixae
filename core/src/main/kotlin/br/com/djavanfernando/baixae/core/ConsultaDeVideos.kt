package br.com.djavanfernando.baixae.core

import java.text.Normalizer

/** Como os Vídeos de uma Biblioteca são filtrados, buscados e ordenados. */
data class ConsultaDeVideos(
    val bibliotecaId: String,
    val plataforma: Plataforma? = null,
    val busca: String = "",
) {
    /** Vídeos da Biblioteca que atendem ao filtro e à busca, do Download mais recente para o mais antigo. */
    fun aplicar(videos: List<Video>): List<Video> {
        val termo = normalizar(busca)
        return videos
            .filter { it.bibliotecaId == bibliotecaId }
            .filter { plataforma == null || it.plataforma == plataforma }
            .filter { termo.isEmpty() || listOf(it.nome, it.autor, it.legenda).any { campo -> termo in normalizar(campo) } }
            .sortedByDescending { it.dataDoDownload }
    }

    private companion object {
        val diacriticos = Regex("""\p{Mn}+""")

        fun normalizar(texto: String) =
            Normalizer.normalize(texto.trim(), Normalizer.Form.NFD).replace(diacriticos, "").lowercase()
    }
}
