package com.seriando.app.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seriando.app.ui.comum.LogoSeriando
import com.seriando.app.ui.legal.AvisoDeTermosEPolitica

/**
 * Tela de entrada (RF53): logotipo, botão de login com Google e opção de entrar como visitante.
 *
 * @param aoEntrarComSucesso      chamada depois do login com Google dar certo.
 * @param aoContinuarComoVisitante chamada ao tocar em "Continuar como visitante" (RF54).
 * @param aoAbrirTermos           chamada ao tocar em "Termos de Uso" no rodapé.
 * @param aoAbrirPrivacidade      chamada ao tocar em "Política de Privacidade" no rodapé.
 *   A tela não conhece a navegação: quem decide o destino é o SeriandoNavHost.
 */
@Composable
fun EntradaScreen(
    aoEntrarComSucesso: () -> Unit,
    aoContinuarComoVisitante: () -> Unit,
    aoAbrirTermos: () -> Unit,
    aoAbrirPrivacidade: () -> Unit
) {
    // Estado e ação do login, compartilhados com a folha inferior do RF54
    val login = rememberLoginComGoogle(aoLogarComSucesso = aoEntrarComSucesso)

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Conteúdo fora das barras do sistema (RNF14)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Topo: logotipo, com espaço flexível acima e abaixo para centralizar no conjunto
        Spacer(modifier = Modifier.weight(1f))
        LogoSeriando(alturaIcone = 64.dp, tamanhoTexto = 44.sp)
        Spacer(modifier = Modifier.weight(1f))

        // Ação principal: login com Google (desabilitado enquanto o login está em andamento)
        Button(
            onClick = login.entrar,
            enabled = !login.entrando,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(if (login.entrando) "Entrando..." else "Entrar com o Google")
        }

        // Ação secundária: segue para a Home sem conta (modo visitante)
        TextButton(
            onClick = aoContinuarComoVisitante,
            enabled = !login.entrando,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("Continuar como visitante")
        }

        // Rodapé: texto com links para a política de privacidade e os termos de uso,
        // que abrem as telas de texto legal dentro do app
        Spacer(modifier = Modifier.height(16.dp))
        AvisoDeTermosEPolitica(
            aoAbrirTermos = aoAbrirTermos,
            aoAbrirPrivacidade = aoAbrirPrivacidade,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}
