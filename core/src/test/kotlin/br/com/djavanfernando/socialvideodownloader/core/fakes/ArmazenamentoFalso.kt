package br.com.djavanfernando.socialvideodownloader.core.fakes

import br.com.djavanfernando.socialvideodownloader.core.portas.ArmazenamentoDeArquivos
import br.com.djavanfernando.socialvideodownloader.core.portas.ArquivoTemporario

/** Armazenamento em memória: arquivos são indexados por "pasta/nomeDoArquivo". */
class ArmazenamentoFalso : ArmazenamentoDeArquivos {
    val arquivos = mutableMapOf<String, String>()
    val temporarios = mutableMapOf<String, String>()
    private var proximoTemporario = 0

    fun escreverTemporario(temporario: ArquivoTemporario, conteudo: String) {
        temporarios[temporario.caminho] = conteudo
    }

    override fun criarTemporario(): ArquivoTemporario =
        ArquivoTemporario("tmp/${proximoTemporario++}.mp4").also { temporarios[it.caminho] = "" }

    override fun descartarTemporario(temporario: ArquivoTemporario) {
        temporarios.remove(temporario.caminho)
    }

    override fun existeNaPasta(pasta: String, nomeDoArquivo: String): Boolean =
        arquivos.containsKey("$pasta/$nomeDoArquivo")

    override fun salvar(temporario: ArquivoTemporario, pasta: String, nomeDoArquivo: String): String {
        val conteudo = temporarios.remove(temporario.caminho) ?: error("Temporário inexistente")
        val caminho = "$pasta/$nomeDoArquivo"
        check(caminho !in arquivos) { "Arquivo já existe: $caminho" }
        arquivos[caminho] = conteudo
        return caminho
    }
}
