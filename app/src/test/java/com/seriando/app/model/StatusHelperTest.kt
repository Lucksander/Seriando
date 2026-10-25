package com.seriando.app.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Testes unitários (JVM, sem emulador) dos status de acompanhamento (RF08).
 * Séries e filmes têm conjuntos diferentes, e o que vai para o Firestore é o "name" do enum.
 */
class StatusHelperTest {

    // ---------- opcoesDeStatus(): os rótulos mostrados nos chips ----------

    @Test
    fun filmeTemTresStatus() {
        assertEquals(listOf("Quero assistir", "Concluído", "Abandonei"), opcoesDeStatus("movie"))
    }

    @Test
    fun serieTemQuatroStatus() {
        assertEquals(
            listOf("Quero assistir", "Assistindo", "Maratonei", "Abandonei"),
            opcoesDeStatus("tv")
        )
    }

    // ---------- labelDoStatus(): do texto salvo para o rótulo ----------

    @Test
    fun rotuloDoStatusDeFilme() {
        assertEquals("Concluído", labelDoStatus("movie", "ASSISTIDO"))
    }

    @Test
    fun rotuloDoStatusDeSerie() {
        assertEquals("Abandonei", labelDoStatus("tv", "ABANDONEI"))
        assertEquals("Assistindo", labelDoStatus("tv", "ASSISTINDO"))
    }

    @Test
    fun textoDesconhecidoOuVazioViraQueroAssistir() {
        assertEquals("Quero assistir", labelDoStatus("tv", ""))
        assertEquals("Quero assistir", labelDoStatus("movie", "QUALQUER_COISA"))
    }

    @Test
    fun filmeTambemPodeSerAbandonado() {
        // Às vezes a pessoa não termina o filme
        assertEquals("Abandonei", labelDoStatus("movie", "ABANDONEI"))
        assertEquals("ABANDONEI", nomeDoStatus("movie", "Abandonei"))
    }

    @Test
    fun statusDeSerieNaoExisteParaFilme() {
        // "ASSISTINDO" só existe em séries: num filme, cai no padrão
        assertEquals("Quero assistir", labelDoStatus("movie", "ASSISTINDO"))
    }

    // ---------- nomeDoStatus(): do rótulo escolhido para o texto salvo ----------

    @Test
    fun nomeSalvoDoStatusDeFilme() {
        assertEquals("ASSISTIDO", nomeDoStatus("movie", "Concluído"))
        assertEquals("QUERO_ASSISTIR", nomeDoStatus("movie", "Quero assistir"))
    }

    @Test
    fun nomeSalvoDoStatusDeSerie() {
        assertEquals("COMPLETO", nomeDoStatus("tv", "Maratonei"))
        assertEquals("ASSISTINDO", nomeDoStatus("tv", "Assistindo"))
    }

    @Test
    fun rotuloDesconhecidoViraQueroAssistir() {
        assertEquals("QUERO_ASSISTIR", nomeDoStatus("tv", "Rótulo que não existe"))
        // "Assistindo" é rótulo de série; para filme, cai no padrão
        assertEquals("QUERO_ASSISTIR", nomeDoStatus("movie", "Assistindo"))
    }

    // ---------- ida e volta: o que a tela mostra volta igual depois de salvar e ler ----------

    @Test
    fun todosOsRotulosFazemIdaEVolta() {
        for (tipo in listOf("tv", "movie")) {
            for (rotulo in opcoesDeStatus(tipo)) {
                val salvo = nomeDoStatus(tipo, rotulo)
                assertEquals("tipo $tipo, rótulo $rotulo", rotulo, labelDoStatus(tipo, salvo))
            }
        }
    }

    // ---------- enums: conversão direta ----------

    @Test
    fun enumsConvertemOTextoSalvo() {
        assertEquals(StatusSerie.COMPLETO, StatusSerie.deTexto("COMPLETO"))
        assertEquals(StatusSerie.QUERO_ASSISTIR, StatusSerie.deTexto("xyz"))
        assertEquals(StatusFilme.ASSISTIDO, StatusFilme.deTexto("ASSISTIDO"))
        assertEquals(StatusFilme.QUERO_ASSISTIR, StatusFilme.deTexto(""))
    }
}
