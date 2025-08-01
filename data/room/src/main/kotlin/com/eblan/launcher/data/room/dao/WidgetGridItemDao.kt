package com.eblan.launcher.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.eblan.launcher.data.room.entity.WidgetGridItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WidgetGridItemDao {
    @Query("SELECT * FROM WidgetGridItemEntity")
    fun getWidgetGridItemEntities(): Flow<List<WidgetGridItemEntity>>

    @Upsert
    suspend fun upsertWidgetGridItemEntities(entities: List<WidgetGridItemEntity>)

    @Upsert
    suspend fun upsertWidgetGridItemEntity(entity: WidgetGridItemEntity): Long

    @Update
    suspend fun updateWidgetGridItemEntity(entity: WidgetGridItemEntity)

    @Query("SELECT * FROM WidgetGridItemEntity WHERE id = :id")
    suspend fun getWidgetGridItemEntity(id: String): WidgetGridItemEntity?

    @Delete
    suspend fun deleteWidgetGridItemEntities(entities: List<WidgetGridItemEntity>)

    @Delete
    suspend fun deleteWidgetGridItemEntity(entity: WidgetGridItemEntity)
}