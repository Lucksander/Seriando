package com.seriando.app.model

// Representa uma denúncia feita sobre uma opinião. Fica numa coleção
// separada (denuncias/{id}), de leitura restrita, nunca lida pelo
// próprio app — só serve para registro e futura moderação.
data class Denuncia(
    val autorOpiniaoUid: String = "", // quem escreveu a opinião denunciada
    val tipoMidia: String = "",
    val tmdbId: Int = 0,
    val denuncianteUid: String = "", // quem denunciou
    val motivo: String = "",
    val data: Long = System.currentTimeMillis()
)

// Motivos fixos de denúncia, para padronizar em vez de texto livre.
val MOTIVOS_DENUNCIA = listOf(
    "Conteúdo ofensivo ou discurso de ódio",
    "Spam ou propaganda",
    "Assédio ou bullying",
    "Informação falsa",
    "Outro"
)