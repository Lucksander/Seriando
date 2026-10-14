package com.seriando.app.data.local

import com.seriando.app.model.SerieSalva

/*
 * Conversões entre a tabela do Room (ListaEntity) e o model que as telas e o Firestore
 * usam (SerieSalva). Ficam aqui para o resto do app nunca precisar conhecer o Room.
 */

/** Converte uma linha do banco local no model que as telas usam. */
fun ListaEntity.paraSerieSalva(): SerieSalva = SerieSalva(
    id = id,
    nome = nome,
    sinopse = sinopse,
    caminhoCapa = caminhoCapa,
    tipoMidia = tipoMidia,
    status = status,
    dataAdicao = dataAdicao,
    atualizadoEm = atualizadoEm
)

/**
 * Converte o model numa linha do banco local.
 *
 * @param pendente     true se a alteração ainda precisa ser enviada ao Firestore (RN22).
 * @param atualizadoEm instante da alteração; por padrão, o que já está no model.
 */
fun SerieSalva.paraEntity(pendente: Boolean, atualizadoEm: Long = this.atualizadoEm): ListaEntity = ListaEntity(
    id = id,
    nome = nome,
    sinopse = sinopse,
    caminhoCapa = caminhoCapa,
    tipoMidia = tipoMidia,
    status = status,
    dataAdicao = dataAdicao,
    atualizadoEm = atualizadoEm,
    pendenteSincronizacao = pendente,
    removido = false
)