package com.seriando.app.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testes unitários (JVM, sem emulador) da escolha das obras do carrossel de destaques (RF84).
 */
class DestaquesDaHomeTest {

    // Obra de exemplo; fundo = null simula uma obra sem imagem horizontal
    private fun obra(id: Int, tipo: String, fundo: String? = "/f$id.jpg") =
        ResultadoBusca(id = id, tipoMidia = tipo, caminhoFundo = fundo)

    @Test
    fun alternaSerieEFilmeComNoMaximoTresDeCada() {
        val series = (1..5).map { obra(it, "tv") }
        val filmes = (11..15).map { obra(it, "movie") }
        val ids = DestaquesDaHome.escolher(series, filmes).map { it.id }
        assertEquals(listOf(1, 11, 2, 12, 3, 13), ids)
    }

    @Test
    fun ignoraObrasSemImagemDeFundo() {
        val series = listOf(obra(1, "tv", fundo = null), obra(2, "tv"))
        val filmes = listOf(obra(11, "movie"))
        val ids = DestaquesDaHome.escolher(series, filmes).map { it.id }
        assertEquals(listOf(2, 11), ids)
    }

    @Test
    fun seUmLadoEstiverVazioUsaSoOOutro() {
        val series = (1..5).map { obra(it, "tv") }
        val ids = DestaquesDaHome.escolher(series, emptyList()).map { it.id }
        assertEquals(listOf(1, 2, 3), ids)
    }

    @Test
    fun semNenhumaObraDevolveListaVazia() {
        assertTrue(DestaquesDaHome.escolher(emptyList(), emptyList()).isEmpty())
    }
}
