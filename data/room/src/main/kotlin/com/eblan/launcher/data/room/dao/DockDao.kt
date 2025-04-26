package com.eblan.launcher.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.eblan.launcher.data.room.entity.DockItemEntity
import com.eblan.launcher.data.room.entity.GridItemEntity
import com.eblan.launcher.domain.model.GridItemData
import kotlinx.coroutines.flow.Flow

@Dao
interface DockDao {
    @Query("SELECT * FROM DockItemEntity")
    fun getGridItemEntities(): Flow<List<DockItemEntity>>

    @Upsert
    suspend fun upsertDockItemEntities(entities: List<DockItemEntity>)

    @Upsert
    suspend fun upsertDockItemEntity(entity: DockItemEntity): Long

    @Query("UPDATE DockItemEntity SET data = :data WHERE id = :id")
    suspend fun updateGridItemData(id: String, data: GridItemData): Int

    @Query("SELECT * FROM DockItemEntity WHERE id = :id")
    suspend fun getDockItemEntity(id: String): DockItemEntity?

    @Delete
    suspend fun deleteDockItemEntity(entity: DockItemEntity)
}