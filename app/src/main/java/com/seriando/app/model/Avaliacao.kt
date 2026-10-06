package com.seriando.app.model


// Representa a avaliação (e, futuramente, a opinião) de um usuário sobre
// uma obra específica. Um único documento por usuário por obra (RN01),
// por isso nota e opinião dividem o mesmo documento no Firestore.
data class Avaliacao(
    val autorUid: String = "",
    val nota: Int = 0, // 1 a 5 (RN02); 0 significa "sem nota ainda"
    val texto: String = "", // usado a partir do Bloco C
    val spoiler: Boolean = false, // usado a partir do Bloco C
    val data: Long = System.currentTimeMillis()
)