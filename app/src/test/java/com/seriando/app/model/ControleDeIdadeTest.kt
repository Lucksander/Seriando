package com.seriando.app.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testes unitários (JVM, sem emulador) do controle de idade (RF50, RF51; RN13 a RN16, RN31).
 */
class ControleDeIdadeTest {

    /** Atalho para montar uma classificação do Brasil pelo rótulo ("L", "10", "12"...). */
    private fun classificacao(rotulo: String): ClassificacaoIndicativa {
        val idade = if (rotulo == "L") 0 else rotulo.toInt()
        return ClassificacaoIndicativa(rotulo, idade)
    }

    // ---------- idadeEfetiva(): visitante e data ausente ou inválida valem 12 (RN16) ----------

    @Test
    fun semDataDeNascimentoValeIdadeRestrita() {
        assertEquals(ControleDeIdade.IDADE_RESTRITA, ControleDeIdade.idadeEfetiva(null))
        assertEquals(12, ControleDeIdade.idadeEfetiva(null))
    }

    @Test
    fun dataInvalidaValeIdadeRestrita() {
        // O aniversário antigo em texto livre ("15/03") e datas impossíveis caem na faixa restrita
        assertEquals(12, ControleDeIdade.idadeEfetiva("15/03"))
        assertEquals(12, ControleDeIdade.idadeEfetiva("2008-02-31"))
        assertEquals(12, ControleDeIdade.idadeEfetiva("2999-01-01"))
    }

    @Test
    fun dataValidaDeAdultoDaIdadeReal() {
        // Quem nasceu em 1990 é adulto em qualquer dia em que o teste rodar
        assertTrue(ControleDeIdade.idadeEfetiva("1990-01-01") >= ControleDeIdade.MAIORIDADE)
    }

    // ---------- permitida(): classificação x idade ----------

    @Test
    fun classificacaoLLiberadaParaTodos() {
        assertTrue(ControleDeIdade.permitida(classificacao("L"), 0))
        assertTrue(ControleDeIdade.permitida(classificacao("L"), 12))
    }

    @Test
    fun faixaRestritaDe12VeLE10E12() {
        val idade = ControleDeIdade.IDADE_RESTRITA
        assertTrue(ControleDeIdade.permitida(classificacao("L"), idade))
        assertTrue(ControleDeIdade.permitida(classificacao("10"), idade))
        assertTrue(ControleDeIdade.permitida(classificacao("12"), idade))
    }

    @Test
    fun faixaRestritaDe12NaoVe14_16E18() {
        val idade = ControleDeIdade.IDADE_RESTRITA
        assertFalse(ControleDeIdade.permitida(classificacao("14"), idade))
        assertFalse(ControleDeIdade.permitida(classificacao("16"), idade))
        assertFalse(ControleDeIdade.permitida(classificacao("18"), idade))
    }

    @Test
    fun idadeIgualAClassificacaoEhPermitida() {
        // O limite é inclusivo: quem tem 16 anos vê classificação 16
        assertTrue(ControleDeIdade.permitida(classificacao("16"), 16))
    }

    @Test
    fun umAnoAbaixoDaClassificacaoNaoEhPermitido() {
        assertFalse(ControleDeIdade.permitida(classificacao("16"), 15))
    }

    @Test
    fun classificacao18SoParaMaioresDeIdade() {
        assertFalse(ControleDeIdade.permitida(classificacao("18"), 17))
        assertTrue(ControleDeIdade.permitida(classificacao("18"), 18))
    }

    // ---------- obra sem classificação (RN14) e consulta que falhou (RN31) ----------

    @Test
    fun obraSemClassificacaoFicaOcultaParaMenoresDe18() {
        assertFalse(ControleDeIdade.permitida(null, 12))
        assertFalse(ControleDeIdade.permitida(null, 17))
    }

    @Test
    fun obraSemClassificacaoApareceParaQuemTem18OuMais() {
        assertTrue(ControleDeIdade.permitida(null, 18))
        assertTrue(ControleDeIdade.permitida(null, 40))
    }

    @Test
    fun visitanteNaoVeObraSemClassificacao() {
        // Fluxo completo do visitante: sem data -> idade 12 -> obra sem classificação oculta
        val idadeDoVisitante = ControleDeIdade.idadeEfetiva(null)
        assertFalse(ControleDeIdade.permitida(null, idadeDoVisitante))
    }
}
