package com.seriando.app.ui.usuario

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.seriando.app.model.ModoDoTema

/**
 * Seção "Aparência" da aba Conta (RF80): três botões de escolha para o tema do app,
 * "Aparelho" (segue o modo do aparelho), "Claro" e "Escuro".
 *
 * Esta peça só mostra e avisa: quem grava a escolha (DataStore) é a UsuarioScreen, e o tema
 * muda na hora porque a MainActivity observa a mesma preferência.
 *
 * @param modoAtual modo hoje escolhido (o botão correspondente aparece selecionado).
 * @param aoEscolher chamada com o modo tocado pelo usuário.
 */
@Composable
fun CartaoDeAparencia(
    modoAtual: ModoDoTema,
    aoEscolher: (ModoDoTema) -> Unit
) {
    CartaoDeSecao(titulo = "Aparência") {
        // Linha de título com a descrição do que está valendo agora
        LinhaDaConta(
            icone = Icons.Outlined.Palette,
            titulo = "Tema",
            valor = when (modoAtual) {
                ModoDoTema.SISTEMA -> "Segue o modo claro ou escuro do aparelho"
                ModoDoTema.CLARO -> "Sempre claro"
                ModoDoTema.ESCURO -> "Sempre escuro"
            }
        )
        // Os três botões dividem a largura do cartão em partes iguais (weight)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModoDoTema.entries.forEach { modo ->
                FilterChip(
                    selected = modo == modoAtual,
                    onClick = { aoEscolher(modo) },
                    modifier = Modifier.weight(1f),
                    label = {
                        Text(
                            text = modo.rotulo,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                )
            }
        }
    }
}
