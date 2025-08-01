package com.eblan.launcher.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.eblan.launcher.data.room.entity.ShortcutInfoGridItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShortcutInfoGridItemDao {
    @Query("SELECT * FROM ShortcutInfoGridItemEntity")
    fun getShortcutInfoGridItemEntities(): Flow<List<ShortcutInfoGridItemEntity>>

    @Upsert
    suspend fun upsertShortcutInfoGridItemEntities(entities: List<ShortcutInfoGridItemEntity>)

    @Upsert
    suspend fun upsertShortcutInfoGridItemEntity(entity: ShortcutInfoGridItemEntity): Long

    @Update
    suspend fun updateShortcutInfoGridItemEntity(entity: ShortcutInfoGridItemEntity)

    @Query("SELECT * FROM ShortcutInfoGridItemEntity WHERE id = :id")
    suspend fun getShortcutInfoGridItemEntity(id: String): ShortcutInfoGridItemEntity?

    @Delete
    suspend fun deleteShortcutInfoGridItemEntities(entities: List<ShortcutInfoGridItemEntity>)

    @Delete
    suspend fun deleteShortcutInfoGridItemEntity(entity: ShortcutInfoGridItemEntity)
}