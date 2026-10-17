package br.com.djavanfernando.baixae.fila

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import br.com.djavanfernando.baixae.BaixaeApp
import java.util.concurrent.TimeUnit

/** Tarefa periódica semanal: atualiza os Extratores e procura uma nova versão do app. */
class VerificacaoSemanalWorker(context: Context, parametros: WorkerParameters) : CoroutineWorker(context, parametros) {
    override suspend fun doWork(): Result {
        val dependencias = (applicationContext as BaixaeApp).dependencias
        val conhecida = dependencias.atualizacoesDoApp.novaVersaoDisponivel()
        try {
            dependencias.verificacaoSemanal.executar()
        } catch (_: Exception) {
            // Sem nova tentativa imediata: a próxima execução semanal tenta de novo.
        }
        val nova = dependencias.atualizacoesDoApp.novaVersaoDisponivel()
        if (nova != null && nova != conhecida) Notificacoes.avisarNovaVersao(applicationContext, nova)
        return Result.success()
    }

    companion object {
        private const val NOME = "verificacao-semanal"

        fun agendar(context: Context) {
            // Atraso inicial: na primeira abertura o app não disputa CPU com a preparação do yt-dlp.
            val pedido = PeriodicWorkRequestBuilder<VerificacaoSemanalWorker>(7, TimeUnit.DAYS)
                .setInitialDelay(1, TimeUnit.DAYS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .setRequiresBatteryNotLow(true)
                        .build(),
                )
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(NOME, ExistingPeriodicWorkPolicy.KEEP, pedido)
        }
    }
}
