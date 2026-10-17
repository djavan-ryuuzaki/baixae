package br.com.djavanfernando.baixae.core.fakes

import br.com.djavanfernando.baixae.core.portas.ArquivoTemporario
import br.com.djavanfernando.baixae.core.portas.ProcessadorDeVideo
import kotlinx.coroutines.CompletableDeferred

/** Processador que "transforma" o conteúdo textual do arquivo; pode ser segurado no meio para testar progresso e cancelamento. */
class ProcessadorFalso(private val armazenamento: ArmazenamentoFalso) : ProcessadorDeVideo {
    var trava: CompletableDeferred<Unit>? = null
    val textosAplicados = mutableListOf<String>()

    override suspend fun aplicarMarcaDagua(
        origem: String,
        destino: ArquivoTemporario,
        texto: String,
        aoProgredir: (Float) -> Unit,
    ) {
        textosAplicados += texto
        val conteudo = armazenamento.arquivos[origem] ?: error("Origem inexistente")
        armazenamento.escreverTemporario(destino, "parcial")
        aoProgredir(0.5f)
        trava?.await()
        armazenamento.escreverTemporario(destino, "marcado($conteudo)")
        aoProgredir(1f)
    }

    override suspend fun removerAudio(origem: String, destino: ArquivoTemporario) {
        val conteudo = armazenamento.arquivos[origem] ?: error("Origem inexistente")
        armazenamento.escreverTemporario(destino, "sem som($conteudo)")
    }
}
