package com.seriando.app.model

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Utilitário da data de nascimento (RF59, RN13, RN17).
 *
 * A data é guardada como texto "AAAA-MM-DD" (ex: "2008-03-15"). Esse formato permite comparar
 * datas como texto e é o mesmo que as regras do Firestore validam. Usa Calendar e
 * SimpleDateFormat, e não java.time, porque o minSdk do app é 24.
 * A idade nunca é guardada: é calculada no momento do uso (RN13).
 */
object DataNascimento {

    /** Primeiro ano aceito; as regras do Firestore usam o mesmo limite (1900-01-01). */
    const val ANO_MINIMO = 1900
    private const val DATA_MINIMA = "1900-01-01"

    private val FORMATO = Regex("""^\d{4}-\d{2}-\d{2}$""")
    private val UTC: TimeZone = TimeZone.getTimeZone("UTC")

    /** Data de hoje no mesmo formato, usada para recusar datas no futuro. */
    private fun hoje(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    /**
     * true se o texto é uma data real, entre 1900-01-01 e hoje.
     * Recusa formato errado, datas que não existem (2008-02-31) e datas futuras.
     */
    fun valida(texto: String?): Boolean {
        if (texto == null || !FORMATO.matches(texto)) return false
        try {
            // isLenient = false: o parser não "arruma" datas impossíveis
            SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }.parse(texto)
        } catch (e: ParseException) {
            return false
        }
        // Como o formato é fixo, comparar como texto equivale a comparar como data
        return texto >= DATA_MINIMA && texto <= hoje()
    }

    /** Idade em anos completos hoje, ou null se a data for inválida ou ausente (RN13). */
    fun idadeEmAnos(texto: String?, hoje: Calendar = Calendar.getInstance()): Int? {
        if (!valida(texto)) return null
        val ano = texto!!.substring(0, 4).toInt()
        val mes = texto.substring(5, 7).toInt()
        val dia = texto.substring(8, 10).toInt()
        var idade = hoje.get(Calendar.YEAR) - ano
        val mesAtual = hoje.get(Calendar.MONTH) + 1 // Calendar conta os meses a partir de 0
        // Ainda não fez aniversário neste ano: tira um ano
        if (mesAtual < mes || (mesAtual == mes && hoje.get(Calendar.DAY_OF_MONTH) < dia)) idade--
        return idade
    }

    /** "2008-03-15" vira "15/03/2008" para exibir; null se a data for inválida. */
    fun paraExibicao(texto: String?): String? {
        if (!valida(texto)) return null
        return "${texto!!.substring(8, 10)}/${texto.substring(5, 7)}/${texto.substring(0, 4)}"
    }

    /** Converte "AAAA-MM-DD" em milissegundos UTC (meia-noite), que é o que o DatePicker usa. */
    fun paraMillisUtc(texto: String?): Long? {
        if (!valida(texto)) return null
        return Calendar.getInstance(UTC).apply {
            clear()
            set(texto!!.substring(0, 4).toInt(), texto.substring(5, 7).toInt() - 1, texto.substring(8, 10).toInt())
        }.timeInMillis
    }

    /** Converte os milissegundos UTC escolhidos no DatePicker em "AAAA-MM-DD". */
    fun deMillisUtc(millis: Long): String {
        val c = Calendar.getInstance(UTC).apply { timeInMillis = millis }
        return "%04d-%02d-%02d".format(
            c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)
        )
    }

    /** O dia de hoje (no fuso do aparelho) como milissegundos UTC: limite das datas selecionáveis. */
    fun hojeEmMillisUtc(): Long {
        val local = Calendar.getInstance()
        return Calendar.getInstance(UTC).apply {
            clear()
            set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
        }.timeInMillis
    }
}