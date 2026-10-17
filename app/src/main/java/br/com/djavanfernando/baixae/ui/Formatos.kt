package br.com.djavanfernando.baixae.ui

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

fun formatarDuracao(segundos: Long): String {
    val horas = segundos / 3600
    val minutos = (segundos % 3600) / 60
    val resto = segundos % 60
    return if (horas > 0) "%d:%02d:%02d".format(horas, minutos, resto) else "%d:%02d".format(minutos, resto)
}

private val formatoDeData = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(Locale.forLanguageTag("pt-BR"))

fun formatarData(instante: Instant): String = formatoDeData.format(instante.atZone(ZoneId.systemDefault()))
