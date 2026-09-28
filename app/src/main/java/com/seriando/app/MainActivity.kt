package com.seriando.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.seriando.app.navigation.SeriandoNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // A partir de agora, quem decide qual tela mostrar
            // é o SeriandoNavHost, não mais uma chamada fixa.
            SeriandoNavHost()
        }
    }
}