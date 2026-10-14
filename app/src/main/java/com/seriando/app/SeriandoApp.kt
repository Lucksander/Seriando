package com.seriando.app

import android.app.Application
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.seriando.app.data.local.Dependencias
import com.seriando.app.data.sync.Sincronizador
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Classe que o Android cria antes de qualquer tela, uma única vez por abertura do app.
 * Prepara o banco local (Room) do modo offline e, se já houver usuário logado, pede a
 * sincronização com a nuvem.
 */
class SeriandoApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Cria o banco local; os repositórios o acessam por Dependencias
        Dependencias.iniciar(this)

        // Teste de fumaça: confirma que o Room abre e responde. Pode ser apagado depois.
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val total = Dependencias.listaDao.contar()
                Log.d("Seriando", "Banco local pronto: $total item(ns) na lista")
            } catch (e: Exception) {
                Log.e("Seriando", "Falha ao abrir o banco local", e)
            }
        }

        // Usuário já logado ao abrir o app: sincroniza (envia pendências e baixa a lista)
        FirebaseAuth.getInstance().currentUser?.uid?.let { Sincronizador.disparar(it) }
    }
}