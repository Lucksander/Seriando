package com.seriando.app.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Testes unitários (JVM, sem emulador) da regra do próximo episódio da fileira
 * "Continuar assistindo" (RF83).
 */
class ContinuarAssistindoTest {

    // Série de exemplo: temporada 1 com 3 episódios e temporada 2 com 2; mais os especiais (0)
    private val temporadas = listOf(
        TemporadaResumo(numero = 0, totalEpisodios = 4, nome = "Especiais"),
        TemporadaResumo(numero = 1, totalEpisodios = 3, nome = "Temporada 1"),
        TemporadaResumo(numero = 2, totalEpisodios = 2, nome = "Temporada 2")
    )

    @Test
    fun semNenhumEpisodioAssistidoComecaNoPrimeiroDaPrimeiraTemporada() {
        assertEquals(ProximoEpisodio(1, 1), ContinuarAssistindoRegras.proximoEpisodio(emptyList(), temporadas))
    }

    @Test
    fun continuaNoEpisodioSeguinteDaMesmaTemporada() {
        val vistos = listOf(1 to 1, 1 to 2)
        assertEquals(ProximoEpisodio(1, 3), ContinuarAssistindoRegras.proximoEpisodio(vistos, temporadas))
    }

    @Test
    fun noFimDaTemporadaVaiParaOPrimeiroDaSeguinte() {
        val vistos = listOf(1 to 1, 1 to 2, 1 to 3)
        assertEquals(ProximoEpisodio(2, 1), ContinuarAssistindoRegras.proximoEpisodio(vistos, temporadas))
    }

    @Test
    fun noUltimoEpisodioDaSerieEstaEmDia() {
        val vistos = listOf(2 to 1, 2 to 2)
        assertNull(ContinuarAssistindoRegras.proximoEpisodio(vistos, temporadas))
    }

    @Test
    fun especiaisNaoContamComoUltimoEpisodio() {
        // Só um especial assistido: ainda é como se não tivesse começado a série
        val vistos = listOf(0 to 3)
        assertEquals(ProximoEpisodio(1, 1), ContinuarAssistindoRegras.proximoEpisodio(vistos, temporadas))
    }

    @Test
    fun usaOMaiorEpisodioAssistidoMesmoForaDeOrdem() {
        val vistos = listOf(2 to 1, 1 to 3, 1 to 1)
        assertEquals(ProximoEpisodio(2, 2), ContinuarAssistindoRegras.proximoEpisodio(vistos, temporadas))
    }

    @Test
    fun semTemporadasNaoHaProximo() {
        assertNull(ContinuarAssistindoRegras.proximoEpisodio(listOf(1 to 1), emptyList()))
    }
}
