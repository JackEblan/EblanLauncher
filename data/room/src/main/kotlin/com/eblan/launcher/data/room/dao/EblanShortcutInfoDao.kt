package com.eblan.launcher.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.eblan.launcher.data.room.entity.EblanShortcutInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EblanShortcutInfoDao {

    @Query("SELECT * FROM EblanShortcutInfoEntity")
    fun getEblanShortcutInfoEntities(): Flow<List<EblanShortcutInfoEntity>>

    @Upsert
    suspend fun upsertEblanShortcutInfoEntities(entities: List<EblanShortcutInfoEntity>)

    @Upsert
    suspend fun upsertEblanShortcutInfoEntity(entity: EblanShortcutInfoEntity)

    @Delete
    suspend fun deleteEblanShortcutInfoEntities(entities: List<EblanShortcutInfoEntity>)

    @Query("SELECT * FROM EblanShortcutInfoEntity WHERE shortcutId = :id")
    suspend fun getEblanShortcutInfoEntity(id: String): EblanShortcutInfoEntity?
}