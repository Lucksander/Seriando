package com.seriando.app.model

// Status de acompanhamento específico para filmes — mais simples que o
// de séries, já que não existe "assistindo aos poucos" ou "abandonei"
// de forma comum para algo com duas horas de duração.
enum class StatusFilme(val label: String) {
    QUERO_ASSISTIR("Quero assistir"),
    ASSISTIDO("Assistido");

    companion object {
        fun deTexto(texto: String): StatusFilme =
            entries.find { it.name == texto } ?: QUERO_ASSISTIR
    }
}