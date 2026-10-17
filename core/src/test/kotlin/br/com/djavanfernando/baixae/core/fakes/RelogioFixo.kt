package br.com.djavanfernando.baixae.core.fakes

import br.com.djavanfernando.baixae.core.portas.Relogio
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

class RelogioFixo(
    var agora: Instant = Instant.parse("2026-10-16T12:00:00Z"),
    override var fuso: ZoneId = ZoneOffset.UTC,
) : Relogio {
    override fun agora(): Instant = agora
}
