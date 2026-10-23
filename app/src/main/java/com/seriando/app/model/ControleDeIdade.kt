package com.seriando.app.model

/**
 * Regra do controle de idade pela classificação indicativa (RF50, RF51; RN13 a RN16).
 *
 * Tudo gira em torno da "idade efetiva" do usuário:
 *  - com data de nascimento válida, é a idade real, calculada na hora (RN13);
 *  - visitante ou usuário sem data válida é tratado como menor, com a faixa restrita de
 *    até 12 anos, ou seja, classificações L, 10 e 12 (RN16).
 *
 * Uma obra é permitida quando a classificação dela é menor ou igual à idade efetiva.
 * Obra sem classificação para o Brasil só é permitida a quem tem 18 anos ou mais (RN14).
 */
object ControleDeIdade {

    /** Idade a partir da qual tudo é permitido, inclusive obras sem classificação. */
    const val MAIORIDADE = 18

    /** Idade efetiva de quem é visitante ou não informou uma data de nascimento válida (RN16). */
    const val IDADE_RESTRITA = 12

    /** Idade efetiva a partir da data guardada ("AAAA-MM-DD"); datas ausentes ou inválidas valem 12. */
    fun idadeEfetiva(dataNascimento: String?): Int =
        DataNascimento.idadeEmAnos(dataNascimento) ?: IDADE_RESTRITA

    /**
     * true se uma obra com esta classificação pode ser mostrada a quem tem esta idade efetiva.
     *
     * @param classificacao classificação do Brasil, ou null se a TMDB não informar (RN14).
     * @param idadeEfetiva  idade calculada por [idadeEfetiva].
     */
    fun permitida(classificacao: ClassificacaoIndicativa?, idadeEfetiva: Int): Boolean =
        if (classificacao == null) idadeEfetiva >= MAIORIDADE
        else classificacao.idadeMinima <= idadeEfetiva
}