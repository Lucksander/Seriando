package com.seriando.app.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

/**
 * Testes unitários (JVM, sem emulador) do utilitário da data de nascimento (RF59, RN13).
 *
 * Para o resultado não depender do dia em que o teste roda, o cálculo da idade recebe um
 * "hoje" fixo (24/10/2026) em vez de usar o relógio do computador.
 */
class DataNascimentoTest {

    /** Monta um Calendar com a data informada; o mês usa as constantes de Calendar (OCTOBER etc.). */
    private fun hoje(ano: Int, mes: Int, dia: Int): Calendar =
        Calendar.getInstance().apply {
            clear()
            set(ano, mes, dia)
        }

    // ---------- valida() ----------

    @Test
    fun aceitaDataRealNoFormatoCerto() {
        assertTrue(DataNascimento.valida("2008-03-15"))
    }

    @Test
    fun aceitaOLimiteMinimoDe1900() {
        // 1900-01-01 é o primeiro dia aceito (o mesmo limite das regras do Firestore)
        assertTrue(DataNascimento.valida("1900-01-01"))
    }

    @Test
    fun recusaDataAnteriorA1900() {
        assertFalse(DataNascimento.valida("1899-12-31"))
    }

    @Test
    fun recusaDataNoFuturo() {
        assertFalse(DataNascimento.valida("2999-01-01"))
    }

    @Test
    fun recusaDiaQueNaoExiste() {
        // O parser não pode "arrumar" a data: 31 de fevereiro não existe
        assertFalse(DataNascimento.valida("2008-02-31"))
    }

    @Test
    fun recusa29DeFevereiroEmAnoNaoBissexto() {
        assertFalse(DataNascimento.valida("2007-02-29"))
    }

    @Test
    fun aceita29DeFevereiroEmAnoBissexto() {
        assertTrue(DataNascimento.valida("2008-02-29"))
    }

    @Test
    fun recusaFormatoErrado() {
        // Formato brasileiro, sem zeros à esquerda, texto livre (o aniversário antigo) e vazio
        assertFalse(DataNascimento.valida("15/03/2008"))
        assertFalse(DataNascimento.valida("2008-3-5"))
        assertFalse(DataNascimento.valida("15/03"))
        assertFalse(DataNascimento.valida(""))
    }

    @Test
    fun recusaNulo() {
        assertFalse(DataNascimento.valida(null))
    }

    // ---------- idadeEmAnos() ----------

    @Test
    fun idadeNoDiaDoAniversarioJaContaOAno() {
        // Nasceu em 24/10/2008 e hoje é 24/10/2026: faz 18 anos hoje
        assertEquals(18, DataNascimento.idadeEmAnos("2008-10-24", hoje(2026, Calendar.OCTOBER, 24)))
    }

    @Test
    fun idadeUmDiaAntesDoAniversarioAindaNaoContaOAno() {
        // Nasceu em 25/10/2008 e hoje é 24/10/2026: ainda tem 17 anos
        assertEquals(17, DataNascimento.idadeEmAnos("2008-10-25", hoje(2026, Calendar.OCTOBER, 24)))
    }

    @Test
    fun idadeUmDiaDepoisDoAniversarioJaContaOAno() {
        assertEquals(18, DataNascimento.idadeEmAnos("2008-10-23", hoje(2026, Calendar.OCTOBER, 24)))
    }

    @Test
    fun idadeComAniversarioEmMesPosteriorAindaNaoContaOAno() {
        // Nasceu em dezembro: em outubro ainda não fez aniversário neste ano
        assertEquals(17, DataNascimento.idadeEmAnos("2008-12-01", hoje(2026, Calendar.OCTOBER, 24)))
    }

    @Test
    fun idadeDeQuemNasceuEm29DeFevereiroFazAniversarioEm1DeMarcoNosAnosNaoBissextos() {
        // Em 28/02/2026 (ano não bissexto) a pessoa de 29/02/2008 ainda tem 17 anos...
        assertEquals(17, DataNascimento.idadeEmAnos("2008-02-29", hoje(2026, Calendar.FEBRUARY, 28)))
        // ...e em 01/03/2026 já tem 18
        assertEquals(18, DataNascimento.idadeEmAnos("2008-02-29", hoje(2026, Calendar.MARCH, 1)))
    }

    @Test
    fun idadeDeDataInvalidaOuAusenteEhNula() {
        assertNull(DataNascimento.idadeEmAnos(null))
        assertNull(DataNascimento.idadeEmAnos("15/03"))
        assertNull(DataNascimento.idadeEmAnos("2999-01-01"))
    }

    // ---------- paraExibicao() ----------

    @Test
    fun exibicaoTrocaParaODiaMesAno() {
        assertEquals("15/03/2008", DataNascimento.paraExibicao("2008-03-15"))
    }

    @Test
    fun exibicaoDeDataInvalidaEhNula() {
        assertNull(DataNascimento.paraExibicao("2008-02-31"))
        assertNull(DataNascimento.paraExibicao(null))
    }

    // ---------- conversão de/para milissegundos UTC (DatePicker) ----------

    @Test
    fun milissegundosDe1970ComecamEmZero() {
        assertEquals(0L, DataNascimento.paraMillisUtc("1970-01-01"))
        assertEquals("1970-01-01", DataNascimento.deMillisUtc(0L))
    }

    @Test
    fun idaEVoltaEmMilissegundosDevolveAMesmaData() {
        val data = "2008-03-15"
        val millis = DataNascimento.paraMillisUtc(data)!!
        assertEquals(data, DataNascimento.deMillisUtc(millis))
    }

    @Test
    fun milissegundosDeDataInvalidaSaoNulos() {
        assertNull(DataNascimento.paraMillisUtc("2999-01-01"))
    }

    @Test
    fun hojeEmMilissegundosUtcEhSempreMeiaNoite() {
        // Um dia tem 86.400.000 ms; a data do DatePicker precisa cair exatamente à meia-noite UTC
        assertEquals(0L, DataNascimento.hojeEmMillisUtc() % 86_400_000L)
    }
}
