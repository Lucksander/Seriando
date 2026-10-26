package com.seriando.app.data.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testes unitários (JVM) da tradução dos códigos de erro do Firebase Auth (RF32, RNF06).
 */
class ErrosDeAuthTest {

    @Test
    fun emailInexistenteESenhaErradaTemAMesmaMensagem() {
        // Proteção contra enumeração: quem tenta entrar não descobre se o e-mail tem conta
        val a = mensagemPorCodigo("ERROR_INVALID_CREDENTIAL", OperacaoDeAuth.ENTRAR)
        val b = mensagemPorCodigo("ERROR_WRONG_PASSWORD", OperacaoDeAuth.ENTRAR)
        val c = mensagemPorCodigo("ERROR_USER_NOT_FOUND", OperacaoDeAuth.ENTRAR)
        assertEquals("E-mail ou senha incorretos.", a)
        assertEquals(a, b)
        assertEquals(a, c)
    }

    @Test
    fun emailEmUsoNaoDizDeQualMetodoEhAConta() {
        val mensagem = mensagemPorCodigo("ERROR_EMAIL_ALREADY_IN_USE", OperacaoDeAuth.CRIAR_CONTA)
        // Aponta os dois caminhos e não menciona Google
        assertTrue(mensagem.contains("Esqueci minha senha"))
        assertFalse(mensagem.contains("Google", ignoreCase = true))
    }

    @Test
    fun errosConhecidosTemMensagemPropria() {
        assertEquals("Informe um e-mail válido.", mensagemPorCodigo("ERROR_INVALID_EMAIL", OperacaoDeAuth.OUTRA))
        assertEquals("Esta conta foi desativada.", mensagemPorCodigo("ERROR_USER_DISABLED", OperacaoDeAuth.ENTRAR))
        assertTrue(mensagemPorCodigo("ERROR_WEAK_PASSWORD", OperacaoDeAuth.CRIAR_CONTA).contains("Senha muito fraca"))
        assertTrue(mensagemPorCodigo("ERROR_TOO_MANY_REQUESTS", OperacaoDeAuth.ENTRAR).contains("Muitas tentativas"))
        assertTrue(mensagemPorCodigo("ERROR_NETWORK_REQUEST_FAILED", OperacaoDeAuth.ENTRAR).contains("Sem conexão"))
        assertTrue(mensagemPorCodigo("ERROR_REQUIRES_RECENT_LOGIN", OperacaoDeAuth.OUTRA).contains("entre de novo"))
    }

    @Test
    fun codigoDesconhecidoOuNuloUsaMensagemGenericaDaOperacao() {
        assertEquals("Não foi possível entrar. Tente de novo.", mensagemPorCodigo(null, OperacaoDeAuth.ENTRAR))
        assertEquals("Não foi possível criar a conta. Tente de novo.", mensagemPorCodigo("ERROR_X", OperacaoDeAuth.CRIAR_CONTA))
        assertEquals("Não foi possível enviar o e-mail. Tente de novo.", mensagemPorCodigo(null, OperacaoDeAuth.REDEFINIR_SENHA))
        assertEquals("Algo deu errado. Tente de novo.", mensagemPorCodigo(null, OperacaoDeAuth.OUTRA))
    }
}
