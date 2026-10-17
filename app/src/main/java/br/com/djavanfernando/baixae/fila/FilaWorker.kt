package br.com.djavanfernando.baixae.fila

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import br.com.djavanfernando.baixae.BaixaeApp
import br.com.djavanfernando.baixae.core.EstadoNaFila
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch

/** Executa a Fila de Downloads em segundo plano, com notificação de progresso, mesmo com o app fechado. */
class FilaWorker(context: Context, parametros: WorkerParameters) : CoroutineWorker(context, parametros) {
    private val dependencias get() = (applicationContext as BaixaeApp).dependencias

    @OptIn(FlowPreview::class)
    override suspend fun doWork(): Result {
        val fila = dependencias.fila
        val concluidosAntes = concluidos()
        try {
            setForeground(getForegroundInfo())
        } catch (_: Exception) {
            // Sem permissão para serviço em primeiro plano agora: a fila anda mesmo assim, sem a notificação fixa.
        }
        coroutineScope {
            val notificador = launch {
                combine(dependencias.repositorio.observarFila(), fila.progresso) { itens, progresso ->
                    Notificacoes.resumir(itens, progresso)
                }.sample(NOTIFICACAO_A_CADA_MS).collect { resumo ->
                    if (resumo.emAndamento > 0) {
                        NotificationManagerCompat.from(applicationContext).notifySeguro(
                            Notificacoes.ID_DA_FILA,
                            Notificacoes.daFila(applicationContext, resumo),
                        )
                    }
                }
            }
            atualizarExtratoresSeVencidos()
            fila.processar()
            notificador.cancel()
        }
        if (fila.haDownloadsAguardandoRede()) AgendadorDaFila.esperarRede(applicationContext, fila.exigeWifi())
        Notificacoes.avisarConclusao(applicationContext, (concluidos() - concluidosAntes).size)
        return Result.success()
    }

    /**
     * O yt-dlp embutido no APK envelhece rápido (o Instagram, por exemplo, para de funcionar com versões antigas).
     * Antes de baixar, atualiza os Extratores se a última verificação passou de uma semana ou nunca aconteceu.
     */
    private suspend fun atualizarExtratoresSeVencidos() {
        try {
            dependencias.extratores.verificacaoSemanal()
        } catch (_: Exception) {
            // Sem rede ou GitHub fora do ar: baixa com a versão atual; a falha, se houver, aparece no Download.
        }
    }

    private suspend fun concluidos(): Set<String> =
        dependencias.repositorio.itensDaFila().filter { it.estado == EstadoNaFila.CONCLUIDO }.map { it.id }.toSet()

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notificacao = Notificacoes.daFila(applicationContext, Notificacoes.ResumoDaFila(0, 0, null))
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(Notificacoes.ID_DA_FILA, notificacao, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(Notificacoes.ID_DA_FILA, notificacao)
        }
    }

    private companion object {
        const val NOTIFICACAO_A_CADA_MS = 1_000L
    }
}

object AgendadorDaFila {
    private const val FILA = "fila-de-downloads"
    private const val ESPERA_DE_REDE = "fila-de-downloads-espera-de-rede"

    /** Garante que a fila esteja andando; chamado sempre que algo é enfileirado. */
    fun iniciar(context: Context) {
        val pedido = OneTimeWorkRequestBuilder<FilaWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(FILA, ExistingWorkPolicy.APPEND_OR_REPLACE, pedido)
    }

    /** Retoma a fila quando houver rede (ou Wi-Fi, se exigido). */
    fun esperarRede(context: Context, exigeWifi: Boolean) {
        val pedido = OneTimeWorkRequestBuilder<FilaWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(if (exigeWifi) NetworkType.UNMETERED else NetworkType.CONNECTED)
                    .build(),
            )
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(ESPERA_DE_REDE, ExistingWorkPolicy.REPLACE, pedido)
    }
}
