package br.com.djavanfernando.baixae.core.fakes

import br.com.djavanfernando.baixae.core.portas.ArmazenamentoDeArquivos
import br.com.djavanfernando.baixae.core.portas.ArquivoSalvo
import br.com.djavanfernando.baixae.core.portas.ArquivoTemporario

/** Armazenamento em memória: arquivos são indexados por "pasta/nomeDoArquivo". */
class ArmazenamentoFalso : ArmazenamentoDeArquivos {
    val arquivos = mutableMapOf<String, String>()
    val temporarios = mutableMapOf<String, String>()

    /** Arquivos que existem na pasta mas o app não enxerga (ex.: de outro app); salvar por cima ganha " (1)", como no MediaStore. */
    val ocultos = mutableSetOf<String>()
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

    override fun salvar(temporario: ArquivoTemporario, pasta: String, nomeDoArquivo: String): ArquivoSalvo {
        val conteudo = temporarios.remove(temporario.caminho) ?: error("Temporário inexistente")
        val nomeUsado = if ("$pasta/$nomeDoArquivo" in ocultos) {
            nomeDoArquivo.substringBeforeLast('.') + " (1)." + nomeDoArquivo.substringAfterLast('.')
        } else {
            nomeDoArquivo
        }
        val caminho = "$pasta/$nomeUsado"
        check(caminho !in arquivos) { "Arquivo já existe: $caminho" }
        arquivos[caminho] = conteudo
        return ArquivoSalvo(caminho, nomeUsado)
    }

    override fun renomear(caminho: String, novoNomeDoArquivo: String): ArquivoSalvo {
        val conteudo = arquivos.remove(caminho) ?: error("Arquivo inexistente: $caminho")
        val novo = caminho.substringBeforeLast('/') + "/" + novoNomeDoArquivo
        check(novo !in arquivos) { "Arquivo já existe: $novo" }
        arquivos[novo] = conteudo
        return ArquivoSalvo(novo, novoNomeDoArquivo)
    }

    var espacoLivre = Long.MAX_VALUE

    override fun espacoLivre(pasta: String): Long = espacoLivre

    override fun existe(caminho: String): Boolean = caminho in arquivos

    override fun apagar(caminho: String) {
        arquivos.remove(caminho)
    }
}
