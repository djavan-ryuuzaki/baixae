package br.com.djavanfernando.baixae.adaptadores

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import br.com.djavanfernando.baixae.core.portas.Conectividade
import br.com.djavanfernando.baixae.core.portas.EstadoDaRede

class ConectividadeAndroid(context: Context) : Conectividade {
    private val gerenciador = context.getSystemService(ConnectivityManager::class.java)

    override fun estado(): EstadoDaRede {
        val capacidades = gerenciador.getNetworkCapabilities(gerenciador.activeNetwork) ?: return EstadoDaRede.SEM_REDE
        if (!capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) return EstadoDaRede.SEM_REDE
        return if (capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)) {
            EstadoDaRede.WIFI
        } else {
            EstadoDaRede.MOVEL
        }
    }
}
