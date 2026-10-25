package com.seriando.app

import android.graphics.Color as CorDoAndroid
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.seriando.app.data.local.PreferenciasApp
import com.seriando.app.model.ModoDoTema
import com.seriando.app.navigation.SeriandoNavHost
import com.seriando.app.ui.comum.ComFaixaDeConexao
import com.seriando.app.ui.theme.SeriandoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Tela de ponta a ponta; antes de o tema carregar, os ícones da barra de status
        // acompanham o modo claro/escuro do aparelho
        enableEdgeToEdge()

        setContent {
            // Modo do tema escolhido na aba Conta (RF80). null = ainda lendo o DataStore
            // (alguns milissegundos); enquanto isso só o fundo aparece, sem montar as telas,
            // para o app não piscar no tema errado.
            val preferencias = remember { PreferenciasApp(applicationContext) }
            val modoSalvo by produceState<ModoDoTema?>(initialValue = null) {
                // collect (e não first): o tema muda na hora quando a escolha é gravada
                preferencias.modoDoTema.collect { value = it }
            }
            val escuro = (modoSalvo ?: ModoDoTema.SISTEMA).escuro(isSystemInDarkTheme())

            // Os ícones das barras de status e de navegação precisam seguir o tema do app, e
            // não o do aparelho; senão ficam claros sobre fundo claro (ou escuros sobre escuro)
            // quando o usuário força um modo diferente do aparelho.
            DisposableEffect(escuro) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        CorDoAndroid.TRANSPARENT, CorDoAndroid.TRANSPARENT
                    ) { escuro },
                    navigationBarStyle = SystemBarStyle.auto(
                        CorDoAndroid.argb(0xe6, 0xFF, 0xFF, 0xFF), // cortina da barra no claro
                        CorDoAndroid.argb(0x80, 0x1b, 0x1b, 0x1b)  // cortina da barra no escuro
                    ) { escuro }
                )
                onDispose {}
            }

            // O tema envolve todo o app. A Surface na raiz dá o fundo e a cor padrão do texto
            // às telas que não ficam dentro de um Scaffold (Busca, Detalhes, Pessoa e Categoria).
            SeriandoTheme(darkTheme = escuro) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Só monta o app depois de ler o modo do tema
                    if (modoSalvo != null) {
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
}
