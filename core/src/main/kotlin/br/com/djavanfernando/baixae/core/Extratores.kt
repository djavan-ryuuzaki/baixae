package br.com.djavanfernando.baixae.core

import br.com.djavanfernando.baixae.core.portas.Extrator
import br.com.djavanfernando.baixae.core.portas.Preferencias
import br.com.djavanfernando.baixae.core.portas.Relogio
import java.time.Duration

/** O que o usuário pode fazer diante de um Download que falhou. Nunca há nova tentativa silenciosa. */
enum class AcaoAposFalha {
    /** A Plataforma mudou: atualizar os Extratores e tentar de novo. */
    ATUALIZAR_EXTRATORES_E_TENTAR,
    TENTAR_DE_NOVO,

    /** Nada a fazer (ex.: Post que exige login). */
    NENHUMA,
    ;

    companion object {
        fun para(tipo: TipoDeFalha): AcaoAposFalha = when (tipo) {
            TipoDeFalha.PLATAFORMA_MUDOU -> ATUALIZAR_EXTRATORES_E_TENTAR
            TipoDeFalha.EXIGE_LOGIN -> NENHUMA
            TipoDeFalha.SEM_REDE, TipoDeFalha.SEM_ESPACO, TipoDeFalha.OUTRO -> TENTAR_DE_NOVO
        }
    }
}

/** Versão e atualização dos Extratores, inclusive a verificação semanal. */
class Extratores(
    private val extrator: Extrator,
    private val fila: FilaDeDownloads,
    private val preferencias: Preferencias,
    private val relogio: Relogio,
) {
    suspend fun versao(): String = extrator.versao()

    suspend fun atualizar(): String = extrator.atualizar()

    /** Botão "Atualizar extratores e tentar de novo" de um Download que falhou. */
    suspend fun atualizarETentarDeNovo(itemId: String) {
        atualizar()
        fila.tentarDeNovo(itemId)
    }

    /** Atualiza os Extratores se a última verificação foi há uma semana ou mais; devolve se atualizou. */
    suspend fun verificacaoSemanal(): Boolean {
        val agora = relogio.agora()
        val configuracoes = preferencias.ler()
        val ultima = configuracoes.ultimaVerificacaoDeExtratores
        if (ultima != null && Duration.between(ultima, agora) < INTERVALO) return false
        atualizar()
        preferencias.salvar(preferencias.ler().copy(ultimaVerificacaoDeExtratores = agora))
        return true
    }

    companion object {
        val INTERVALO: Duration = Duration.ofDays(7)
    }
}
