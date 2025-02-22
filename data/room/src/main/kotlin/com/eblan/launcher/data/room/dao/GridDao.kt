package com.eblan.launcher.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.eblan.launcher.data.room.entity.GridItemEntity
import com.eblan.launcher.domain.model.GridItemType
import kotlinx.coroutines.flow.Flow

@Dao
interface GridDao {
    @Query("SELECT * FROM GridItemEntity")
    fun getGridItemEntities(): Flow<List<GridItemEntity>>

    @Upsert
    suspend fun upsertGridItemEntities(gridItemEntities: List<GridItemEntity>)

    @Upsert
    suspend fun upsertGridItemEntity(gridItemEntity: GridItemEntity)

    @Query("UPDATE GridItemEntity SET type = :type WHERE id = :id")
    suspend fun updateGridItemType(id: Int, type: GridItemType)
}