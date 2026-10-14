package com.seriando.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.seriando.app.data.local.Dependencias
import com.seriando.app.data.local.PerfilDao
import com.seriando.app.data.local.PerfilEntity
import com.seriando.app.data.sync.Sincronizador
import com.seriando.app.model.PerfilPrivado
import com.seriando.app.model.PerfilPublico
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** O perfil completo do usuário: a parte pública (apelido e avatar) e a privada (e-mail e aniversário). */
data class PerfilCompleto(
    val publico: PerfilPublico,
    val privado: PerfilPrivado
)

/**
 * Perfil do usuário (RF12 a RF15), offline-first (RF33).
 *
 * O perfil mora numa única linha do banco local, que a sincronização reparte nos dois documentos
 * do Firestore: perfisPublicos/{uid} (apelido e avatar) e usuarios/{uid} (aniversário).
 * As telas leem e gravam sempre no banco local (RN21); cada alteração fica marcada como pendente
 * (RN22) e o Sincronizador a envia em segundo plano.
 *
 * Quem cria o perfil padrão do primeiro acesso é o Sincronizador, depois de conferir a nuvem.
 * Se esta classe o criasse ao não achar o perfil, um banco local vazio (depois de sair e entrar,
 * por exemplo) sobrescreveria o apelido real do usuário.
 *
 * @param local DAO do perfil (interface do Room, RNF11).
 */
class PerfilRepository(
    private val local: PerfilDao = Dependencias.perfilDao
) {

    /** O perfil completo, atualizado sozinho quando o banco local mudar. null enquanto ainda não existe. */
    fun observarPerfil(uid: String): Flow<PerfilCompleto?> =
        local.observar(uid).map { entidade -> entidade?.let { paraPerfilCompleto(it) } }

    /** O perfil público, observado: usado pela Home para mostrar nome e foto. */
    fun observarPerfilPublico(uid: String): Flow<PerfilPublico?> =
        local.observar(uid).map { entidade ->
            entidade?.let { PerfilPublico(apelido = it.apelido, avatarId = it.avatarId) }
        }

    /** Leitura única do perfil público, do banco local. */
    suspend fun buscarPerfilPublico(uid: String): PerfilPublico? =
        local.obter(uid)?.let { PerfilPublico(apelido = it.apelido, avatarId = it.avatarId) }

    /** Leitura única do perfil privado, do banco local. O e-mail vem do login do Google. */
    suspend fun buscarPerfilPrivado(uid: String): PerfilPrivado? =
        local.obter(uid)?.let {
            PerfilPrivado(email = emailDoLogin(), aniversario = it.aniversario)
        }

    /** Grava apelido e avatar, preservando o aniversário. Pendente de envio. */
    suspend fun salvarPerfilPublico(uid: String, perfil: PerfilPublico) {
        val atual = local.obter(uid)
        local.salvar(
            PerfilEntity(
                uid = uid,
                apelido = perfil.apelido,
                avatarId = perfil.avatarId,
                aniversario = atual?.aniversario,
                atualizadoEm = System.currentTimeMillis(),
                pendenteSincronizacao = true
            )
        )
        // Só envia: baixar a nuvem a cada alteração geraria leituras demais
        Sincronizador.disparar(uid, baixarDaNuvem = false)
    }

    /** Grava o aniversário, preservando apelido e avatar. Pendente de envio. */
    suspend fun salvarPerfilPrivado(uid: String, perfil: PerfilPrivado) {
        val atual = local.obter(uid)
        local.salvar(
            PerfilEntity(
                uid = uid,
                // "Usuário" só entra se o perfil ainda não existia: o apelido nunca fica vazio,
                // porque as regras do Firestore recusariam o envio
                apelido = atual?.apelido ?: "Usuário",
                avatarId = atual?.avatarId ?: "google",
                aniversario = perfil.aniversario,
                atualizadoEm = System.currentTimeMillis(),
                pendenteSincronizacao = true
            )
        )
        Sincronizador.disparar(uid, baixarDaNuvem = false)
    }

    private fun paraPerfilCompleto(entidade: PerfilEntity) = PerfilCompleto(
        publico = PerfilPublico(apelido = entidade.apelido, avatarId = entidade.avatarId),
        privado = PerfilPrivado(email = emailDoLogin(), aniversario = entidade.aniversario)
    )

    /** E-mail da conta Google logada (não é guardado no banco local: coleta mínima, RNF08). */
    private fun emailDoLogin(): String = FirebaseAuth.getInstance().currentUser?.email.orEmpty()
}