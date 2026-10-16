package br.com.djavanfernando.socialvideodownloader.core

import br.com.djavanfernando.socialvideodownloader.core.portas.ArmazenamentoDeArquivos
import java.time.LocalDate

/** Regras de nome de Vídeo (o nome é o nome do arquivo, sem a extensão). */
object NomeDeVideo {
    private val invalidos = Regex("""[\\/:*?"<>|\p{Cntrl}]+""")
    private const val TAMANHO_MAXIMO_DO_AUTOR = 60

    /** Nome padrão `{plataforma}_{autor}_{data}`. */
    fun padrao(plataforma: Plataforma, autor: String, data: LocalDate): String {
        val autorLimpo = sanitizar(autor).take(TAMANHO_MAXIMO_DO_AUTOR).trim().ifEmpty { "desconhecido" }
        return "${plataforma.name.lowercase()}_${autorLimpo}_$data"
    }

    /** Troca caracteres que não podem aparecer em nomes de arquivo e normaliza espaços. */
    fun sanitizar(nome: String): String =
        nome.replace(Regex("""\s+"""), " ").trim()
            .replace(invalidos, "_")
            .trim('_', ' ', '.')

    /** [base], ou `base_2`, `base_3`… — o primeiro que ainda não existe em [pasta]. */
    fun livre(base: String, pasta: String, armazenamento: ArmazenamentoDeArquivos): String =
        generateSequence(1) { it + 1 }
            .map { if (it == 1) base else "${base}_$it" }
            .first { !armazenamento.existeNaPasta(pasta, "$it.${Video.EXTENSAO}") }
}
