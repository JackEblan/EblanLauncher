package com.eblan.launcher.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.eblan.launcher.data.room.entity.GridItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GridDao {
    @Query("SELECT * FROM GridItemEntity")
    fun getGridItemEntities(): Flow<List<GridItemEntity>>

    @Upsert
    suspend fun updateGridItemEntities(gridItemEntities: List<GridItemEntity>)
}