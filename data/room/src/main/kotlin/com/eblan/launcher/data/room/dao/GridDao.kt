package com.eblan.launcher.data.room.dao

import androidx.room.Dao
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
    suspend fun upsertGridItemEntity(entity: GridItemEntity)

    @Query("UPDATE GridItemEntity SET data = :data WHERE id = :id")
    suspend fun updateGridItemData(id: Int, data: GridItemData)
}