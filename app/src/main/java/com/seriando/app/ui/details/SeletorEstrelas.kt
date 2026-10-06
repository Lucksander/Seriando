package com.seriando.app.ui.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

// Componente de 5 estrelas tocáveis. notaAtual vai de 0 (nenhuma estrela)
// a 5. Tocar na estrela N define a nota como N — não dá para dar meia
// estrela, conforme RN02 (nota é um inteiro de 1 a 5).
@Composable
fun SeletorEstrelas(notaAtual: Int, aoSelecionar: (Int) -> Unit) {
    Row {
        for (posicao in 1..5) {
            val preenchida = posicao <= notaAtual
            Text(
                text = if (preenchida) "★" else "☆",
                fontSize = 32.sp,
                modifier = Modifier.clickable { aoSelecionar(posicao) }
            )
        }
    }
}