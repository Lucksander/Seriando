package com.seriando.app.model

/**
 * Regras de validação dos campos do cadastro e do login por e-mail e senha (RF32).
 *
 * São funções puras (sem Android nem Firebase), então valem igualmente na tela, que mostra o
 * erro embaixo do campo, e nos testes da JVM. Cada função devolve a mensagem de erro em português,
 * ou null quando o valor é válido. A validação final de verdade é a do Firebase; isto só evita
 * uma ida à internet por um erro que dá para ver na hora.
 */
object ValidacaoDeConta {

    /** Tamanho mínimo da senha. */
    const val SENHA_MINIMA = 8

    /** Tamanho máximo da senha (o Firebase aceita mais; o limite evita textos enormes). */
    const val SENHA_MAXIMA = 64

    /** Tamanho máximo do apelido: o mesmo limite das regras do Firestore (RNF01). */
    const val APELIDO_MAXIMO = 40

    /** Tamanho máximo de um e-mail, pelo padrão da internet (RFC 5321). */
    private const val EMAIL_MAXIMO = 254

    // Formato simples: algo@dominio.ext, com letras, números e . _ % + ' - antes do @.
    // Não tenta cobrir todos os casos raros do padrão; quem decide se o e-mail existe é o
    // envio da confirmação.
    private val FORMATO_DO_EMAIL =
        Regex("^[A-Za-z0-9._%+'-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")

    /** Erro do e-mail digitado, ou null se estiver válido. Espaços nas pontas são ignorados. */
    fun erroDoEmail(email: String): String? {
        val texto = email.trim()
        return when {
            texto.isEmpty() -> "Informe seu e-mail."
            texto.length > EMAIL_MAXIMO || !FORMATO_DO_EMAIL.matches(texto) -> "Informe um e-mail válido."
            else -> null
        }
    }

    /**
     * Erro da senha escolhida no cadastro, ou null se estiver válida: de 8 a 64 caracteres,
     * com pelo menos uma letra e um número. Espaços fazem parte da senha e não são removidos.
     */
    fun erroDaSenha(senha: String): String? = when {
        senha.isEmpty() -> "Informe uma senha."
        senha.length < SENHA_MINIMA -> "A senha precisa ter pelo menos $SENHA_MINIMA caracteres."
        senha.length > SENHA_MAXIMA -> "A senha pode ter no máximo $SENHA_MAXIMA caracteres."
        senha.none { it.isLetter() } || senha.none { it.isDigit() } -> "Use letras e números na senha."
        else -> null
    }

    /** Erro da repetição da senha, ou null se for igual à senha digitada. */
    fun erroDaConfirmacao(senha: String, confirmacao: String): String? =
        if (senha == confirmacao) null else "As senhas não são iguais."

    /** Erro do apelido, ou null se estiver válido (1 a 40 caracteres, sem contar espaços nas pontas). */
    fun erroDoApelido(apelido: String): String? {
        val texto = apelido.trim()
        return when {
            texto.isEmpty() -> "Escolha um apelido."
            texto.length > APELIDO_MAXIMO -> "O apelido pode ter no máximo $APELIDO_MAXIMO caracteres."
            else -> null
        }
    }
}
