package com.seriando.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Banco local do Seriando (Room), usado pelo modo offline (RF33).
 * Declara as tabelas e entrega os DAOs. Não é criado diretamente: quem o cria,
 * uma única vez, é a classe Dependencias.
 *
 * exportSchema = false: o esquema não é exportado para arquivo (projeto sem migrações).
 * Se uma tabela mudar depois de o app ser publicado, será preciso subir a versão e
 * escrever uma migração.
 */
@Database(
    entities = [
        ListaEntity::class,
        EpisodioAssistidoEntity::class,
        PerfilEntity::class,
        ObraCacheEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SeriandoDatabase : RoomDatabase() {
    abstract fun listaDao(): ListaDao
    abstract fun episodioDao(): EpisodioDao
    abstract fun perfilDao(): PerfilDao
    abstract fun obraCacheDao(): ObraCacheDao
}