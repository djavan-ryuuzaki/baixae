package br.com.djavanfernando.socialvideodownloader.core.portas

/** Arquivo de trabalho, fora da Pasta de destino, onde o Extrator escreve antes de o Vídeo ser salvo. */
data class ArquivoTemporario(val caminho: String)

/** Arquivos no armazenamento do celular, organizados em Pastas de destino. */
interface ArmazenamentoDeArquivos {
    fun criarTemporario(): ArquivoTemporario
    fun descartarTemporario(temporario: ArquivoTemporario)
    fun existeNaPasta(pasta: String, nomeDoArquivo: String): Boolean

    /** Move o temporário para a Pasta de destino e devolve o caminho do arquivo salvo. */
    fun salvar(temporario: ArquivoTemporario, pasta: String, nomeDoArquivo: String): String

    /** Renomeia o arquivo dentro da mesma pasta e devolve o novo caminho. */
    fun renomear(caminho: String, novoNomeDoArquivo: String): String

    fun existe(caminho: String): Boolean

    /** Apaga o arquivo; não falha se ele já não existir. */
    fun apagar(caminho: String)
}
