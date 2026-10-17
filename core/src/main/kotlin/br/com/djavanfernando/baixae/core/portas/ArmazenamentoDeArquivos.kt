package br.com.djavanfernando.baixae.core.portas

/** Arquivo de trabalho, fora da Pasta de destino, onde o Extrator escreve antes de o Vídeo ser salvo. */
data class ArquivoTemporario(val caminho: String)

/**
 * Arquivo gravado numa Pasta de destino. O [nomeDoArquivo] é o que o armazenamento de fato usou: ele pode
 * diferir do pedido quando já existe um arquivo invisível para o app com o mesmo nome.
 */
data class ArquivoSalvo(val caminho: String, val nomeDoArquivo: String)

/** Arquivos no armazenamento do celular, organizados em Pastas de destino. */
interface ArmazenamentoDeArquivos {
    fun criarTemporario(): ArquivoTemporario
    fun descartarTemporario(temporario: ArquivoTemporario)
    fun existeNaPasta(pasta: String, nomeDoArquivo: String): Boolean

    /** Move o temporário para a Pasta de destino. */
    fun salvar(temporario: ArquivoTemporario, pasta: String, nomeDoArquivo: String): ArquivoSalvo

    /** Renomeia o arquivo dentro da mesma pasta. */
    fun renomear(caminho: String, novoNomeDoArquivo: String): ArquivoSalvo

    /** Bytes livres para gravar em [pasta] (incluindo o temporário do Download). */
    fun espacoLivre(pasta: String): Long

    fun existe(caminho: String): Boolean

    /** Apaga o arquivo; não falha se ele já não existir. */
    fun apagar(caminho: String)
}
