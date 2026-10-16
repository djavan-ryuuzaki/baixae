package br.com.djavanfernando.socialvideodownloader.core.portas

import br.com.djavanfernando.socialvideodownloader.core.LimiteDeQualidade
import br.com.djavanfernando.socialvideodownloader.core.Post

/** Componente que sabe obter vídeos de uma Plataforma. */
interface Extrator {
    suspend fun obterPost(link: String): Post

    /** Baixa a mídia de [indice] do Post para [destino]; cancelável pelo cancelamento da coroutine. */
    suspend fun baixar(
        link: String,
        indice: Int,
        destino: ArquivoTemporario,
        limite: LimiteDeQualidade,
        aoProgredir: (Float) -> Unit,
    )
}
