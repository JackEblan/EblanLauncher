package com.eblan.launcher.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.eblan.launcher.data.room.entity.GridItemEntity
import com.eblan.launcher.domain.model.GridItemData
import kotlinx.coroutines.flow.Flow

@Dao
interface GridDao {
    @Query("SELECT * FROM GridItemEntity")
    fun getGridItemEntities(): Flow<List<GridItemEntity>>

    @Upsert
    suspend fun upsertGridItemEntities(entities: List<GridItemEntity>)

    @Upsert
    suspend fun upsertGridItemEntity(entity: GridItemEntity): Long

    @Query("UPDATE GridItemEntity SET data = :data WHERE id = :id")
    suspend fun updateGridItemData(id: String, data: GridItemData): Int

    @Query("SELECT * FROM GridItemEntity WHERE id = :id")
    suspend fun getGridItemEntity(id: String): GridItemEntity?

    @Delete
    suspend fun deleteGridItemEntity(entity: GridItemEntity)

    @Query("DELETE FROM GridItemEntity WHERE dataId IN (:dataIds)")
    suspend fun deleteGridItemEntitiesByDataIds(dataIds: List<String>)
}