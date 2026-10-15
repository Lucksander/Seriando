package com.seriando.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.seriando.app.navigation.SeriandoNavHost
import com.seriando.app.ui.comum.ComFaixaDeConexao
import com.seriando.app.ui.theme.SeriandoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Tela de ponta a ponta; os ícones da barra de status acompanham o modo claro/escuro do aparelho
        enableEdgeToEdge()

        setContent {
            // O tema envolve todo o app. A Surface na raiz dá o fundo e a cor padrão do texto
            // às telas que não ficam dentro de um Scaffold (Busca, Detalhes, Pessoa e Categoria).
            SeriandoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // A faixa de conexão (RF62) fica acima de todas as telas.
                    // Quem decide qual tela mostrar é o SeriandoNavHost.
                    ComFaixaDeConexao {
                        SeriandoNavHost()
                    }
                }
            }
        }
    }
}