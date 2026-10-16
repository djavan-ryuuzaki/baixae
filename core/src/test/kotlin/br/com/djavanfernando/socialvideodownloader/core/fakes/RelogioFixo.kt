package br.com.djavanfernando.socialvideodownloader.core.fakes

import br.com.djavanfernando.socialvideodownloader.core.portas.Relogio
import java.time.Instant

class RelogioFixo(var agora: Instant = Instant.parse("2026-10-16T12:00:00Z")) : Relogio {
    override fun agora(): Instant = agora
}
