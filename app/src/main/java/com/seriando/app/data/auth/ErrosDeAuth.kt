package com.seriando.app.data.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException

/** Em qual tela o erro aconteceu; a mesma falha do Firebase pede mensagens diferentes. */
enum class OperacaoDeAuth { ENTRAR, CRIAR_CONTA, REDEFINIR_SENHA, OUTRA }

/**
 * Traduz uma falha do Firebase Auth para uma mensagem em português para o usuário (RNF06).
 *
 * Com a proteção contra enumeração de e-mails ativada no Firebase, "e-mail inexistente" e
 * "senha errada" chegam como o mesmo erro, e o app também não os distingue: a mensagem do login
 * é sempre "E-mail ou senha incorretos" (spoofing e enumeração, STRIDE).
 */
fun mensagemDeErroDeAuth(erro: Throwable, operacao: OperacaoDeAuth): String = when (erro) {
    is FirebaseNetworkException -> mensagemPorCodigo("ERROR_NETWORK_REQUEST_FAILED", operacao)
    is FirebaseAuthException -> mensagemPorCodigo(erro.errorCode, operacao, detalhe = erro.errorCode)
    // Erro de outro tipo (ex: falha interna do Firebase): mostra o tipo e o começo da mensagem
    // técnica, para dar para descobrir a causa sem abrir o Logcat
    else -> mensagemPorCodigo(
        null, operacao,
        detalhe = "${erro.javaClass.simpleName}: ${erro.message.orEmpty().take(120)}"
    )
}

/**
 * Parte pura da tradução, em função do código do erro (ex: "ERROR_WEAK_PASSWORD").
 * Fica separada para ser testada na JVM, sem precisar criar exceções do Firebase.
 *
 * @param detalhe informação técnica do erro; só é mostrada, entre parênteses, quando o erro não
 *                tem mensagem própria (cai na mensagem genérica). Null não mostra nada.
 */
fun mensagemPorCodigo(codigo: String?, operacao: OperacaoDeAuth, detalhe: String? = null): String = when (codigo) {
    // Login: e-mail inexistente e senha errada têm a mesma resposta, de propósito
    "ERROR_INVALID_CREDENTIAL", "ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND" ->
        "E-mail ou senha incorretos."

    "ERROR_INVALID_EMAIL" -> "Informe um e-mail válido."

    // Cadastro com e-mail que já tem conta (inclusive conta Google). A mensagem aponta os
    // dois caminhos para a pessoa recuperar o acesso, sem confirmar de qual método é a conta.
    "ERROR_EMAIL_ALREADY_IN_USE" ->
        "Não foi possível criar a conta com esse e-mail. Se você já tem conta, " +
                "entre com ela ou use \"Esqueci minha senha\"."

    "ERROR_WEAK_PASSWORD" -> "Senha muito fraca. Use pelo menos 8 caracteres, com letras e números."
    "ERROR_USER_DISABLED" -> "Esta conta foi desativada."
    "ERROR_TOO_MANY_REQUESTS" -> "Muitas tentativas. Aguarde alguns minutos e tente de novo."
    "ERROR_NETWORK_REQUEST_FAILED" -> "Sem conexão com a internet. Tente de novo quando estiver online."
    "ERROR_REQUIRES_RECENT_LOGIN" -> "Por segurança, entre de novo para continuar."

    // Erro de configuração, não do usuário: o método E-mail/senha está desligado no console do Firebase
    "ERROR_OPERATION_NOT_ALLOWED" ->
        "O login por e-mail e senha não está ativado neste app. Avise o desenvolvedor."

    else -> {
        val base = when (operacao) {
            OperacaoDeAuth.ENTRAR -> "Não foi possível entrar. Tente de novo."
            OperacaoDeAuth.CRIAR_CONTA -> "Não foi possível criar a conta. Tente de novo."
            OperacaoDeAuth.REDEFINIR_SENHA -> "Não foi possível enviar o e-mail. Tente de novo."
            OperacaoDeAuth.OUTRA -> "Algo deu errado. Tente de novo."
        }
        if (detalhe.isNullOrBlank()) base else "$base ($detalhe)"
    }
}
