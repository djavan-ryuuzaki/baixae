package br.com.djavanfernando.socialvideodownloader.core.portas

import java.time.Instant

interface Relogio {
    fun agora(): Instant
}
