package com.seriando.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.seriando.app.data.local.Dependencias
import com.seriando.app.data.local.PerfilDao
import com.seriando.app.data.local.PerfilEntity
import com.seriando.app.data.sync.Sincronizador
import com.seriando.app.model.DataNascimento
import com.seriando.app.model.PerfilPrivado
import com.seriando.app.model.PerfilPublico
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * O perfil completo do usuário: a parte pública (apelido e avatar), a privada (e-mail e data de
 * nascimento) e a marca de que a única correção da data já foi usada (RN17).
 */
data class PerfilCompleto(
    val publico: PerfilPublico,
    val privado: PerfilPrivado,
    val aniversarioCorrigido: Boolean = false
)

/** Resultado de tentar gravar a data de nascimento (RF59, RN17). */
enum class ResultadoDataNascimento {
    SALVA,      // primeira data informada (ou a mesma data de novo)
    CORRIGIDA,  // a única correção permitida foi usada; a data agora está travada
    TRAVADA,    // já houve a correção: a data não pode mais mudar
    INVALIDA    // texto que não é uma data válida, ou perfil ainda não disponível
}

/**
 * Perfil do usuário (RF12 a RF15, RF59), offline-first (RF33).
 *
 * O perfil mora numa única linha do banco local, que a sincronização reparte nos dois documentos
 * do Firestore: perfisPublicos/{uid} (apelido e avatar) e usuarios/{uid} (data de nascimento).
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

    /**
     * Grava apelido e avatar, preservando a data de nascimento e a marca de correção
     * (copy() só troca o que é informado). Pendente de envio.
     */
    suspend fun salvarPerfilPublico(uid: String, perfil: PerfilPublico) {
        val atual = local.obter(uid)
        val agora = System.currentTimeMillis()
        local.salvar(
            atual?.copy(
                apelido = perfil.apelido,
                avatarId = perfil.avatarId,
                atualizadoEm = agora,
                pendenteSincronizacao = true
            ) ?: PerfilEntity(
                uid = uid,
                apelido = perfil.apelido,
                avatarId = perfil.avatarId,
                aniversario = null,
                atualizadoEm = agora,
                pendenteSincronizacao = true
            )
        )
        // Só envia: baixar a nuvem a cada alteração geraria leituras demais
        Sincronizador.disparar(uid, baixarDaNuvem = false)
    }

    /**
     * Grava a data de nascimento (RF59) aplicando a trava do RN17:
     *  - sem data válida salva (nunca informou, ou só tem o texto antigo): grava qualquer data válida;
     *  - já tem data válida e a correção ainda não foi usada: grava e marca a correção como usada;
     *  - já tem data válida e a correção foi usada: recusa (TRAVADA).
     * As regras do Firestore repetem essa trava no servidor.
     *
     * @param data data no formato "AAAA-MM-DD".
     */
    suspend fun salvarDataNascimento(uid: String, data: String): ResultadoDataNascimento {
        if (!DataNascimento.valida(data)) return ResultadoDataNascimento.INVALIDA
        // Sem perfil local ainda (primeira sincronização em andamento): não há onde gravar
        val atual = local.obter(uid) ?: return ResultadoDataNascimento.INVALIDA

        val jaTemData = DataNascimento.valida(atual.aniversario)
        // Mesma data de novo: nada a gravar e a correção não é gasta
        if (jaTemData && atual.aniversario == data) return ResultadoDataNascimento.SALVA
        // Mudar uma data já salva só é permitido uma vez
        if (jaTemData && atual.aniversarioCorrigido) return ResultadoDataNascimento.TRAVADA

        local.salvar(
            atual.copy(
                aniversario = data,
                aniversarioCorrigido = jaTemData, // true só quando isto é a correção
                atualizadoEm = System.currentTimeMillis(),
                pendenteSincronizacao = true
            )
        )
        Sincronizador.disparar(uid, baixarDaNuvem = false)
        return if (jaTemData) ResultadoDataNascimento.CORRIGIDA else ResultadoDataNascimento.SALVA
    }

    private fun paraPerfilCompleto(entidade: PerfilEntity) = PerfilCompleto(
        publico = PerfilPublico(apelido = entidade.apelido, avatarId = entidade.avatarId),
        privado = PerfilPrivado(email = emailDoLogin(), aniversario = entidade.aniversario),
        aniversarioCorrigido = entidade.aniversarioCorrigido
    )

    /** E-mail da conta Google logada (não é guardado no banco local: coleta mínima, RNF08). */
    private fun emailDoLogin(): String = FirebaseAuth.getInstance().currentUser?.email.orEmpty()
}