package br.com.djavanfernando.socialvideodownloader.adaptadores

import br.com.djavanfernando.socialvideodownloader.core.TipoDeFalha
import kotlin.test.Test
import kotlin.test.assertEquals

class ClassificadorDeFalhasTest {
    @Test
    fun `mensagens tipicas do yt-dlp viram o tipo de falha certo`() {
        val casos = mapOf(
            "ERROR: [youtube] abc: Sign in to confirm your age. This video may be inappropriate for some users." to TipoDeFalha.EXIGE_LOGIN,
            "ERROR: [Instagram] C1: Requested content is not available, rate-limit reached or login required" to TipoDeFalha.EXIGE_LOGIN,
            "ERROR: [youtube] abc: Private video. Sign in if you've been granted access" to TipoDeFalha.EXIGE_LOGIN,
            "ERROR: Unable to download webpage: <urlopen error [Errno 7] No address associated with hostname>" to TipoDeFalha.SEM_REDE,
            "ERROR: unable to write data: [Errno 28] No space left on device" to TipoDeFalha.SEM_ESPACO,
            "ERROR: [facebook] 123: Unable to extract video data" to TipoDeFalha.PLATAFORMA_MUDOU,
            "WARNING: [youtube] nsig extraction failed: You may experience throttling" to TipoDeFalha.PLATAFORMA_MUDOU,
            "ERROR: algo inesperado" to TipoDeFalha.OUTRO,
            "ERROR: [youtube] abc: Sign in to confirm you're not a bot. Use --cookies-from-browser or --cookies for the authentication." to TipoDeFalha.OUTRO,
            "ERROR: [twitter] 1: HTTP Error 429: Too Many Requests. Use --cookies to log in" to TipoDeFalha.OUTRO,
        )
        casos.forEach { (mensagem, esperado) -> assertEquals(esperado, ClassificadorDeFalhas.classificar(mensagem), mensagem) }
    }
}
