package com.seriando.app.ui.theme

import androidx.compose.ui.graphics.Color

// ---------- Identidade visual "Velvet Purple" ----------

// Tema ESCURO (principal): exatamente a paleta da marca
val VelvetPurple = Color(0xFFA98AF5)           // cor principal: símbolo, elementos ativos, progresso
val Midnight = Color(0xFF17151F)               // fundo principal
val VelvetSurface = Color(0xFF282333)          // cartões, campos e áreas elevadas
val TextoPrincipalEscuro = Color(0xFFF7F5FC)   // títulos e informações importantes
val TextoSecundarioEscuro = Color(0xFFC9C4D3)  // sinopses, metadados e informações complementares

// Tema CLARO: derivado da marca, com lilás mais vivo. O roxo é mais escuro que o do tema
// escuro porque o #A98AF5 sobre fundo claro tem contraste baixo (cerca de 2,7:1); o #7C4DFF
// com texto branco dá cerca de 4,8:1, e ainda é um violeta saturado.
val PrimariaClara = Color(0xFF7C4DFF)
// Fundo com toque de lilás; os cartões (SuperficieClara) são brancos para destacar do fundo
val FundoClaro = Color(0xFFF5EEFF)
val SuperficieClara = Color(0xFFFFFFFF)
// Texto com tom de violeta bem escuro: mais quente, sem perder contraste
val TextoPrincipalClaro = Color(0xFF1E1233)
val TextoSecundarioClaro = Color(0xFF5E4F7E)

// Lilás brilhante: fio fino ao redor das capas das obras
val LilasBrilhante = Color(0xFFC7ADFF)