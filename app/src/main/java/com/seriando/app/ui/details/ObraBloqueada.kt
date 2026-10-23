package com.seriando.app.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.seriando.app.model.ClassificacaoIndicativa

/**
 * Aviso mostrado no lugar da tela de Detalhes quando a obra tem classificação indicativa acima
 * da idade do usuário (RF51, RN14, RN16). Não mostra nenhum dado da obra além da classificação.
 *
 * @param classificacao classificação do Brasil da obra, ou null se a TMDB não informar
 *                      (obra sem classificação só é liberada a maiores de 18 anos).
 * @param aoVoltar      volta para a tela anterior.
 */
@Composable
fun ObraBloqueada(
    classificacao: ClassificacaoIndicativa?,
    aoVoltar: () -> Unit
) {
    // fillMaxWidth (e não fillMaxSize): esta tela fica dentro de uma coluna que rola
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // O selo só aparece quando a classificação é conhecida
        if (classificacao != null) {
            SeloDeClassificacao(classificacao)
        }

        Text(
            text = "Conteúdo indisponível para a sua idade",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Text(
            text = if (classificacao != null) {
                "Esta obra tem classificação indicativa acima da faixa liberada para o seu perfil."
            } else {
                "Esta obra não tem classificação indicativa no Brasil e só é liberada para maiores de 18 anos."
            },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        // Orientação para quem ainda não informou a data de nascimento (visitante ou sem data)
        Text(
            text = "Se você ainda não informou sua data de nascimento, entre na sua conta e informe na aba Conta.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = aoVoltar) { Text("Voltar") }
    }
}