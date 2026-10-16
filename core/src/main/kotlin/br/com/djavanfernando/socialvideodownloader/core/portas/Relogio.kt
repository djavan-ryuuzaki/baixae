package br.com.djavanfernando.socialvideodownloader.core.portas

import java.time.Instant
import java.time.ZoneId

interface Relogio {
    fun agora(): Instant

    /** Fuso do celular, usado para datas mostradas ao usuário (ex.: no nome padrão do Vídeo). */
    val fuso: ZoneId
}
