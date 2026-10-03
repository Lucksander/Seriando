package com.seriando.app.model

// Status de acompanhamento de uma série/filme na lista pessoal do usuário.
// Guardado no Firestore como texto (o "name" do enum) e convertido de volta
// na hora de exibir, usando o rótulo (label) em português.
enum class StatusSerie(val label: String) {
    QUERO_ASSISTIR("Quero assistir"),
    ASSISTINDO("Assistindo"),
    COMPLETO("Completo"),
    ABANDONEI("Abandonei");

    companion object {
        // Converte o texto salvo no Firestore de volta para o enum.
        // Se vier vazio ou um valor desconhecido, assume QUERO_ASSISTIR.
        fun deTexto(texto: String): StatusSerie =
            entries.find { it.name == texto } ?: QUERO_ASSISTIR
    }
}