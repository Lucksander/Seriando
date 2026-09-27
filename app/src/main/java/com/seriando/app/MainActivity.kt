package com.seriando.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SeriandoApp()
        }
    }
}

@Composable
fun SeriandoApp() {
    HomeScreen()
}

@Composable
fun HomeScreen() {

    var textoPesquisa by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "SERIANDO",
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "Organize suas séries. Nunca perca onde parou.",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = textoPesquisa,
            onValueChange = { textoPesquisa = it },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Pesquisar série")
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                println("Pesquisa: $textoPesquisa")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Pesquisar")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Minhas séries",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Você ainda não adicionou nenhuma série.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}