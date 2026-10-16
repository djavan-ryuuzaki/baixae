package br.com.djavanfernando.socialvideodownloader.core

import br.com.djavanfernando.socialvideodownloader.core.portas.Conectividade
import br.com.djavanfernando.socialvideodownloader.core.portas.EstadoDaRede
import br.com.djavanfernando.socialvideodownloader.core.portas.Preferencias
import br.com.djavanfernando.socialvideodownloader.core.portas.Relogio
import br.com.djavanfernando.socialvideodownloader.core.portas.Repositorio
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.UUID

enum class EstadoNaFila(val encerrado: Boolean = false) {
    PENDENTE,
    AGUARDANDO_REDE,
    EM_ANDAMENTO,
    CONCLUIDO(encerrado = true),
    FALHOU(encerrado = true),
    CANCELADO(encerrado = true),
}

/** Um Download na Fila de Downloads. */
data class ItemDaFila(
    val id: String,
    val link: String,
    val plataforma: Plataforma,
    val pasta: String,
    val estado: EstadoNaFila,
    val criadoEm: Instant,
    val titulo: String? = null,
    val falha: TipoDeFalha? = null,
    val videoIds: List<String> = emptyList(),
)

/**
 * Fila de Downloads: até [MAXIMO_SIMULTANEOS] Downloads ao mesmo tempo, espera de rede sem contar como falha,
 * cancelamento e nenhuma nova tentativa automática após falha.
 */
class FilaDeDownloads(
    private val repositorio: Repositorio,
    private val baixador: Baixador,
    private val conectividade: Conectividade,
    private val preferencias: Preferencias,
    private val relogio: Relogio,
) {
    private val trava = Mutex()
    private val emAndamento = mutableMapOf<String, Job>()
    private val acordar = Channel<Unit>(Channel.CONFLATED)
    private val _progresso = MutableStateFlow<Map<String, Float>>(emptyMap())

    /** Progresso (0 a 1) dos Downloads em andamento, por id do item. */
    val progresso: StateFlow<Map<String, Float>> = _progresso

    suspend fun enfileirar(link: String, pasta: String? = null): ItemDaFila {
        val plataforma = Plataforma.reconhecer(link) ?: throw LinkNaoSuportado(link)
        val item = ItemDaFila(
            id = UUID.randomUUID().toString(),
            link = link,
            plataforma = plataforma,
            pasta = pasta ?: preferencias.ler().pastaPadrao,
            estado = EstadoNaFila.PENDENTE,
            criadoEm = relogio.agora(),
        )
        repositorio.salvarItem(item)
        acordar.trySend(Unit)
        return item
    }

    /** Executa a fila até não haver nada que possa andar agora (vazia, ou só itens esperando rede). */
    suspend fun processar() = coroutineScope {
        val aguardandoNestaRodada = mutableSetOf<String>()
        while (true) {
            if (trava.withLock { iniciarProximo(this, aguardandoNestaRodada) }) continue
            if (trava.withLock { emAndamento.isEmpty() }) break
            acordar.receive()
        }
    }

    suspend fun cancelar(id: String) {
        trava.withLock {
            val job = emAndamento[id]
            if (job != null) {
                job.cancel()
                return
            }
            val item = repositorio.itemDaFila(id) ?: return
            if (!item.estado.encerrado) repositorio.salvarItem(item.copy(estado = EstadoNaFila.CANCELADO))
        }
    }

    /** Nova tentativa pedida pelo usuário para um Download que falhou (nunca automática). */
    suspend fun tentarDeNovo(id: String) {
        trava.withLock {
            val item = repositorio.itemDaFila(id) ?: return
            if (item.estado != EstadoNaFila.FALHOU) return
            if (item.falha != null && AcaoAposFalha.para(item.falha) == AcaoAposFalha.NENHUMA) return
            repositorio.salvarItem(item.copy(estado = EstadoNaFila.PENDENTE, falha = null))
        }
        acordar.trySend(Unit)
    }

    /** Tira da fila um item já encerrado (concluído, falhou ou cancelado). */
    suspend fun remover(id: String) {
        val item = repositorio.itemDaFila(id) ?: return
        if (item.estado.encerrado) repositorio.removerItem(id)
    }

    /** Devolve à fila os Downloads que estavam em andamento quando o processo do app terminou. */
    suspend fun retomarInterrompidos() = trava.withLock {
        repositorio.itensDaFila()
            .filter { it.estado == EstadoNaFila.EM_ANDAMENTO && it.id !in emAndamento }
            .forEach { repositorio.salvarItem(it.copy(estado = EstadoNaFila.PENDENTE)) }
    }

    suspend fun haDownloadsAguardandoRede(): Boolean =
        repositorio.itensDaFila().any { it.estado == EstadoNaFila.AGUARDANDO_REDE }

    suspend fun exigeWifi(): Boolean = preferencias.ler().soWifi

    private suspend fun iniciarProximo(escopo: CoroutineScope, aguardandoNestaRodada: MutableSet<String>): Boolean {
        if (emAndamento.size >= MAXIMO_SIMULTANEOS) return false
        val candidatos = repositorio.itensDaFila()
            .filter { it.estado == EstadoNaFila.PENDENTE || it.estado == EstadoNaFila.AGUARDANDO_REDE }
            .filter { it.id !in emAndamento && it.id !in aguardandoNestaRodada }
            .sortedBy { it.criadoEm }
        val proximo = candidatos.firstOrNull() ?: return false
        val configuracoes = preferencias.ler()
        if (!redePermite(configuracoes)) {
            candidatos.filter { it.estado == EstadoNaFila.PENDENTE }
                .forEach { repositorio.salvarItem(it.copy(estado = EstadoNaFila.AGUARDANDO_REDE)) }
            return false
        }
        val iniciado = proximo.copy(estado = EstadoNaFila.EM_ANDAMENTO, falha = null)
        repositorio.salvarItem(iniciado)
        emAndamento[iniciado.id] = escopo.launch {
            executar(iniciado, configuracoes, aguardandoNestaRodada)
        }
        return true
    }

    private suspend fun executar(inicial: ItemDaFila, configuracoes: Configuracoes, aguardando: MutableSet<String>) {
        var item = inicial
        try {
            val videos = baixador.baixar(
                link = item.link,
                pasta = item.pasta,
                limite = configuracoes.limiteDeQualidade,
                aoObterPost = { post ->
                    item = item.copy(titulo = post.legenda.ifBlank { post.autor })
                    repositorio.salvarItem(item)
                },
                aoProgredir = { valor -> _progresso.update { it + (inicial.id to valor) } },
            )
            item = item.copy(estado = EstadoNaFila.CONCLUIDO, videoIds = videos.map { it.id })
        } catch (cancelamento: CancellationException) {
            item = item.copy(estado = EstadoNaFila.CANCELADO)
            throw cancelamento
        } catch (falha: FalhaDoExtrator) {
            item = if (falha.tipo == TipoDeFalha.SEM_REDE) {
                aguardando += item.id
                item.copy(estado = EstadoNaFila.AGUARDANDO_REDE)
            } else {
                item.copy(estado = EstadoNaFila.FALHOU, falha = falha.tipo)
            }
        } catch (_: Throwable) {
            // Inclui Errors (ex.: falha ao carregar o Extrator): o item não pode ficar "em andamento" para sempre.
            item = item.copy(estado = EstadoNaFila.FALHOU, falha = TipoDeFalha.OUTRO)
        } finally {
            withContext(NonCancellable) {
                trava.withLock {
                    repositorio.salvarItem(item)
                    emAndamento.remove(item.id)
                    _progresso.update { it - item.id }
                }
                acordar.trySend(Unit)
            }
        }
    }

    private fun redePermite(configuracoes: Configuracoes): Boolean = when (conectividade.estado()) {
        EstadoDaRede.SEM_REDE -> false
        EstadoDaRede.MOVEL -> !configuracoes.soWifi
        EstadoDaRede.WIFI -> true
    }

    companion object {
        const val MAXIMO_SIMULTANEOS = 2
    }
}
