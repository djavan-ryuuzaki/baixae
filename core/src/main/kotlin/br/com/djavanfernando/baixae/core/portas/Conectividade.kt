package br.com.djavanfernando.baixae.core.portas

enum class EstadoDaRede { SEM_REDE, MOVEL, WIFI }

interface Conectividade {
    fun estado(): EstadoDaRede
}
