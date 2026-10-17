package br.com.djavanfernando.baixae.core.portas

import br.com.djavanfernando.baixae.core.LimiteDeQualidade
import br.com.djavanfernando.baixae.core.Post

/**
 * Componente que sabe obter vídeos de uma Plataforma.
 * Falhas conhecidas são lançadas como [br.com.djavanfernando.baixae.core.FalhaDoExtrator].
 */
interface Extrator {
    /** Metadados do Post; o tamanho estimado de cada vídeo considera o [limite] de qualidade. */
    suspend fun obterPost(link: String, limite: LimiteDeQualidade): Post

    /** Baixa a mídia de [indice] do Post para [destino]; cancelável pelo cancelamento da coroutine. */
    suspend fun baixar(
        link: String,
        indice: Int,
        destino: ArquivoTemporario,
        limite: LimiteDeQualidade,
        aoProgredir: (Float) -> Unit,
    )

    /** Atualiza os Extratores e devolve a nova versão. */
    suspend fun atualizar(): String

    suspend fun versao(): String
}
