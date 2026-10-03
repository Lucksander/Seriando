package com.seriando.app.model

// Função utilitária: devolve o rótulo correto do status salvo,
// escolhendo entre StatusSerie ou StatusFilme de acordo com o tipoMidia.
// Centralizar essa decisão aqui evita repetir a mesma checagem
// em cada tela que precisa mostrar o status.
fun labelDoStatus(tipoMidia: String, statusTexto: String): String {
    return if (tipoMidia == "movie") {
        StatusFilme.deTexto(statusTexto).label
    } else {
        StatusSerie.deTexto(statusTexto).label
    }
}

// Lista de rótulos possíveis para o tipo de mídia informado,
// usada para montar os filtros e os chips de seleção de status.
fun opcoesDeStatus(tipoMidia: String): List<String> {
    return if (tipoMidia == "movie") {
        StatusFilme.entries.map { it.label }
    } else {
        StatusSerie.entries.map { it.label }
    }
}

// Converte um rótulo escolhido na tela de volta para o texto que
// deve ser salvo no Firestore (o "name" do enum correto).
fun nomeDoStatus(tipoMidia: String, label: String): String {
    return if (tipoMidia == "movie") {
        StatusFilme.entries.find { it.label == label }?.name ?: StatusFilme.QUERO_ASSISTIR.name
    } else {
        StatusSerie.entries.find { it.label == label }?.name ?: StatusSerie.QUERO_ASSISTIR.name
    }
}