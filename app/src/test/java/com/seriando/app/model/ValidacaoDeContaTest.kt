package com.seriando.app.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Testes unitários (JVM, sem emulador) das regras de validação do cadastro e do login (RF32).
 */
class ValidacaoDeContaTest {

    // ---------- e-mail ----------

    @Test
    fun aceitaEmailsComuns() {
        assertNull(ValidacaoDeConta.erroDoEmail("lucas@gmail.com"))
        assertNull(ValidacaoDeConta.erroDoEmail("lucas.santos+seriando@mail.uniruy.edu.br"))
        assertNull(ValidacaoDeConta.erroDoEmail("o'brien@empresa.com"))
    }

    @Test
    fun ignoraEspacosNasPontasDoEmail() {
        assertNull(ValidacaoDeConta.erroDoEmail("  lucas@gmail.com  "))
    }

    @Test
    fun recusaEmailVazio() {
        assertEquals("Informe seu e-mail.", ValidacaoDeConta.erroDoEmail(""))
        assertEquals("Informe seu e-mail.", ValidacaoDeConta.erroDoEmail("   "))
    }

    @Test
    fun recusaEmailMalFormado() {
        val invalidos = listOf(
            "lucas", "lucas@", "@gmail.com", "lucas@gmail", "lucas@gmail.",
            "lucas@@gmail.com", "luc as@gmail.com", "lucas@gmail.c", "lucas@.com"
        )
        for (email in invalidos) {
            assertEquals("e-mail: $email", "Informe um e-mail válido.", ValidacaoDeConta.erroDoEmail(email))
        }
    }

    @Test
    fun recusaEmailLongoDemais() {
        val enorme = "a".repeat(250) + "@gmail.com"
        assertNotNull(ValidacaoDeConta.erroDoEmail(enorme))
    }

    // ---------- senha ----------

    @Test
    fun aceitaSenhaComLetrasENumeros() {
        assertNull(ValidacaoDeConta.erroDaSenha("seriando2026"))
        assertNull(ValidacaoDeConta.erroDaSenha("Abc12345"))
    }

    @Test
    fun recusaSenhaVazia() {
        assertEquals("Informe uma senha.", ValidacaoDeConta.erroDaSenha(""))
    }

    @Test
    fun recusaSenhaCurta() {
        // 7 caracteres: um a menos que o mínimo
        assertNotNull(ValidacaoDeConta.erroDaSenha("abc1234"))
        // 8 caracteres passam
        assertNull(ValidacaoDeConta.erroDaSenha("abc12345"))
    }

    @Test
    fun recusaSenhaLongaDemais() {
        assertNull(ValidacaoDeConta.erroDaSenha("a1" + "x".repeat(ValidacaoDeConta.SENHA_MAXIMA - 2)))
        assertNotNull(ValidacaoDeConta.erroDaSenha("a1" + "x".repeat(ValidacaoDeConta.SENHA_MAXIMA - 1)))
    }

    @Test
    fun exigeLetraENumero() {
        assertEquals("Use letras e números na senha.", ValidacaoDeConta.erroDaSenha("somenteletras"))
        assertEquals("Use letras e números na senha.", ValidacaoDeConta.erroDaSenha("1234567890"))
    }

    @Test
    fun espacosFazemParteDaSenha() {
        // 8 caracteres contando os espaços, com letra e número
        assertNull(ValidacaoDeConta.erroDaSenha("ab 12 cd"))
    }

    // ---------- confirmação da senha ----------

    @Test
    fun confirmacaoPrecisaSerIgual() {
        assertNull(ValidacaoDeConta.erroDaConfirmacao("abc12345", "abc12345"))
        assertEquals("As senhas não são iguais.", ValidacaoDeConta.erroDaConfirmacao("abc12345", "abc12346"))
        // Maiúsculas contam
        assertNotNull(ValidacaoDeConta.erroDaConfirmacao("abc12345", "ABC12345"))
    }

    // ---------- apelido ----------

    @Test
    fun apelidoValidoTemDe1a40Caracteres() {
        assertNull(ValidacaoDeConta.erroDoApelido("Lu"))
        assertNull(ValidacaoDeConta.erroDoApelido("x".repeat(40)))
        assertNotNull(ValidacaoDeConta.erroDoApelido("x".repeat(41)))
    }

    @Test
    fun apelidoSoComEspacosEhVazio() {
        assertEquals("Escolha um apelido.", ValidacaoDeConta.erroDoApelido("   "))
        assertEquals("Escolha um apelido.", ValidacaoDeConta.erroDoApelido(""))
    }

    @Test
    fun apelidoNaoContaEspacosNasPontas() {
        // 40 letras com espaços em volta: o texto "de verdade" tem 40, então vale
        assertNull(ValidacaoDeConta.erroDoApelido("  " + "x".repeat(40) + "  "))
    }
}
