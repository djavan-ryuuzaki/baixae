package br.com.djavanfernando.baixae.fila

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import br.com.djavanfernando.baixae.MainActivity
import br.com.djavanfernando.baixae.R
import br.com.djavanfernando.baixae.core.EstadoNaFila
import br.com.djavanfernando.baixae.core.ItemDaFila
import br.com.djavanfernando.baixae.core.portas.VersaoPublicada

object Notificacoes {
    const val ID_DA_FILA = 1
    private const val ID_DE_CONCLUSAO = 2
    private const val ID_DE_NOVA_VERSAO = 3
    private const val CANAL_DA_FILA = "fila"
    private const val CANAL_DE_AVISOS = "avisos"

    data class ResumoDaFila(val emAndamento: Int, val pendentes: Int, val progresso: Float?)

    fun resumir(itens: List<ItemDaFila>, progresso: Map<String, Float>): ResumoDaFila {
        val ativos = itens.filter { it.estado == EstadoNaFila.EM_ANDAMENTO }
        val media = ativos.mapNotNull { progresso[it.id] }.takeIf { it.isNotEmpty() }?.average()?.toFloat()
        return ResumoDaFila(
            emAndamento = ativos.size,
            pendentes = itens.count { it.estado == EstadoNaFila.PENDENTE || it.estado == EstadoNaFila.AGUARDANDO_REDE },
            progresso = media,
        )
    }

    fun criarCanais(context: Context) {
        val gerenciador = context.getSystemService(NotificationManager::class.java)
        gerenciador.createNotificationChannel(
            NotificationChannel(CANAL_DA_FILA, context.getString(R.string.canal_fila), NotificationManager.IMPORTANCE_LOW),
        )
        gerenciador.createNotificationChannel(
            NotificationChannel(CANAL_DE_AVISOS, context.getString(R.string.canal_avisos), NotificationManager.IMPORTANCE_DEFAULT),
        )
    }

    fun daFila(context: Context, resumo: ResumoDaFila): Notification {
        val texto = if (resumo.pendentes > 0) {
            context.getString(R.string.notificacao_fila_texto, resumo.emAndamento, resumo.pendentes)
        } else {
            context.resources.getQuantityString(R.plurals.notificacao_fila_em_andamento, resumo.emAndamento, resumo.emAndamento)
        }
        return NotificationCompat.Builder(context, CANAL_DA_FILA)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(context.getString(R.string.notificacao_fila_titulo))
            .setContentText(texto)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(abrirApp(context))
            .setProgress(100, ((resumo.progresso ?: 0f) * 100).toInt(), resumo.progresso == null)
            .build()
    }

    fun avisarConclusao(context: Context, quantidade: Int) {
        if (quantidade <= 0) return
        val notificacao = NotificationCompat.Builder(context, CANAL_DE_AVISOS)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(context.resources.getQuantityString(R.plurals.notificacao_concluidos, quantidade, quantidade))
            .setContentIntent(abrirApp(context))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notifySeguro(ID_DE_CONCLUSAO, notificacao)
    }

    /** Aviso avulso (ex.: "Adicionado à fila"), usado quando não há interface aberta. */
    fun avisar(context: Context, id: Int, titulo: String, texto: String? = null, acao: PendingIntent? = null) {
        val notificacao = NotificationCompat.Builder(context, CANAL_DE_AVISOS)
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setContentIntent(acao ?: abrirApp(context))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notifySeguro(id, notificacao)
    }

    fun avisarNovaVersao(context: Context, versao: VersaoPublicada) {
        val pagina = PendingIntent.getActivity(
            context,
            1,
            Intent(Intent.ACTION_VIEW, Uri.parse(versao.endereco)),
            PendingIntent.FLAG_IMMUTABLE,
        )
        avisar(
            context,
            ID_DE_NOVA_VERSAO,
            context.getString(R.string.nova_versao_titulo, versao.versao),
            context.getString(R.string.nova_versao_texto),
            pagina,
        )
    }

    private fun abrirApp(context: Context): PendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_IMMUTABLE,
    )
}

/** Notifica só se o usuário permitir notificações (no Android 13+ isso inclui a permissão em tempo de execução). */
@SuppressLint("MissingPermission")
fun NotificationManagerCompat.notifySeguro(id: Int, notificacao: Notification) {
    if (areNotificationsEnabled()) notify(id, notificacao)
}
