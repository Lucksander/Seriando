package com.seriando.app.model

// Resumo dos votos de uma opinião: quantas curtidas, quantas descurtidas,
// e qual foi o voto do usuário atual (1 = curtiu, -1 = descurtiu, null = não votou).
data class VotoResumo(
    val curtidas: Int = 0,
    val descurtidas: Int = 0,
    val meuVoto: Int? = null
)