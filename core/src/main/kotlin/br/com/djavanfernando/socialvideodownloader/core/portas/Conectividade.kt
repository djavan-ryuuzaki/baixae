package br.com.djavanfernando.socialvideodownloader.core.portas

enum class EstadoDaRede { SEM_REDE, MOVEL, WIFI }

interface Conectividade {
    fun estado(): EstadoDaRede
}
