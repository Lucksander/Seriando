package com.seriando.app.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Acompanha se o aparelho tem internet (RF62). Expõe um estado que as telas observam e que
 * serve para disparar a sincronização quando a conexão volta.
 *
 * "Tem internet" quer dizer: a rede ativa chega à internet E o Android já a validou
 * (uma rede de Wi-Fi que pede login, por exemplo, não conta).
 * Usa a permissão ACCESS_NETWORK_STATE, que é comum e não pergunta nada ao usuário.
 */
object MonitorConexao {

    // Começa como "online" até iniciar() ler o estado real
    private val _online = MutableStateFlow(true)

    /** true se há internet agora. Muda sozinho quando a conexão cai ou volta. */
    val online: StateFlow<Boolean> = _online.asStateFlow()

    @Volatile
    private var iniciado = false

    /** Começa a acompanhar a rede. Chamado uma única vez, em SeriandoApp.onCreate. */
    fun iniciar(contexto: Context) {
        if (iniciado) return
        iniciado = true

        val gerente = contexto.applicationContext.getSystemService(ConnectivityManager::class.java) ?: return

        // Estado de partida: a rede ativa neste momento
        _online.value = gerente.getNetworkCapabilities(gerente.activeNetwork)?.temInternet() ?: false

        // O Android avisa quando a rede padrão muda de condição ou some
        gerente.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, capacidades: NetworkCapabilities) {
                _online.value = capacidades.temInternet()
            }

            override fun onLost(network: Network) {
                _online.value = false
            }
        })
    }

    /** A rede chega à internet e já foi validada pelo Android. */
    private fun NetworkCapabilities.temInternet(): Boolean =
        hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}