package com.seriando.app.model

// Representa uma emoção que o autor pode marcar ao escrever uma opinião.
// Usamos substantivos (Raiva, Tristeza...) em vez de adjetivos (raivoso/a,
// triste) para não depender de concordância de gênero.
// cor é um valor ARGB (ex: 0xFFC62828), usado na bolinha indicadora.
data class Emocao(val id: String, val label: String, val cor: Long)

val EMOCOES_DISPONIVEIS = listOf(
    Emocao("curiosidade", "Curiosidade", 0xFF2E7D32),
    Emocao("reflexao", "Reflexão", 0xFF2E7D32),
    Emocao("comocao", "Comoção", 0xFF2E7D32),
    Emocao("nostalgia", "Nostalgia", 0xFF2E7D32),
    Emocao("inspiracao", "Inspiração", 0xFF2E7D32),
    Emocao("alegria", "Alegria", 0xFFF9A825),
    Emocao("diversao", "Diversão", 0xFFF9A825),
    Emocao("surpresa", "Surpresa", 0xFFF9A825),
    Emocao("paixao", "Paixão", 0xFFF9A825),
    Emocao("ansiedade", "Ansiedade", 0xFFEF6C00),
    Emocao("tensao", "Tensão", 0xFFEF6C00),
    Emocao("confusao", "Confusão", 0xFFEF6C00),
    Emocao("raiva", "Raiva", 0xFFC62828),
    Emocao("irritacao", "Irritação", 0xFFC62828),
    Emocao("medo", "Medo", 0xFFC62828),
    Emocao("choque", "Choque", 0xFFC62828),
    Emocao("tristeza", "Tristeza", 0xFF1565C0),
    Emocao("frustracao", "Frustração", 0xFF1565C0),
    Emocao("decepcao", "Decepção", 0xFF1565C0),
    Emocao("tedio", "Tédio", 0xFF1565C0),
    Emocao("intensidade", "Intensidade", 0xFFE91E63),
    Emocao("empolgacao", "Empolgação", 0xFFE91E63),
    Emocao("euforia", "Euforia", 0xFFE91E63),
    Emocao("indiferenca", "Indiferença", 0xFF616161)
)

const val MAXIMO_EMOCOES_POR_OPINIAO = 6