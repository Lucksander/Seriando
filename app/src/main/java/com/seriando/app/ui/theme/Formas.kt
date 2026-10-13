package com.seriando.app.ui.theme

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * Forma única das capas das obras (RNF10): bordas arredondadas de 8dp.
 * Para mudar o raio em todo o app, basta alterar o valor aqui.
 */
val FormaCapa = RoundedCornerShape(8.dp)

/**
 * Acabamento padrão das capas das obras: cantos arredondados e um fio fino de 1dp
 * em lilás brilhante. Usada em todas as telas que mostram capa, para ficarem iguais.
 * Para mudar a cor, altere LilasBrilhante em Color.kt; para a espessura, o valor abaixo.
 */
fun Modifier.capaDeObra(): Modifier =
    this
        .clip(FormaCapa)
        .border(width = 2.dp, color = LilasBrilhante, shape = FormaCapa)