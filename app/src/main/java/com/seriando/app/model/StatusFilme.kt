package com.seriando.app.model

// Status de acompanhamento específico para filmes: mais simples que o de séries, sem
// "assistindo" (um filme não tem episódios para ir marcando). Tem "Abandonei" porque às vezes
// a gente começa um filme e não termina. O "name" do enum é o texto gravado no Firestore
// (as regras já aceitam ABANDONEI, que também é usado em séries).
enum class StatusFilme(val label: String) {
    QUERO_ASSISTIR("Quero assistir"),
    // O "name" (ASSISTIDO) é o texto gravado no Firestore e não muda; só o rótulo mostrado é "Concluído"
    ASSISTIDO("Concluído"),
    ABANDONEI("Abandonei");

    companion object {
        fun deTexto(texto: String): StatusFilme =
            entries.find { it.name == texto } ?: QUERO_ASSISTIR
    }
}