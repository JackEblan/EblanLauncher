package com.eblan.launcher.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.eblan.launcher.data.room.entity.ApplicationInfoGridItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ApplicationInfoGridItemDao {
    @Query("SELECT * FROM ApplicationInfoGridItemEntity")
    fun getApplicationInfoGridItemEntities(): Flow<List<ApplicationInfoGridItemEntity>>

    @Upsert
    suspend fun upsertApplicationInfoGridItemEntities(entities: List<ApplicationInfoGridItemEntity>)

    @Upsert
    suspend fun upsertApplicationInfoGridItemEntity(entity: ApplicationInfoGridItemEntity): Long

    @Update
    suspend fun updateApplicationInfoGridItemEntity(entity: ApplicationInfoGridItemEntity)

    @Query("SELECT * FROM ApplicationInfoGridItemEntity WHERE id = :id")
    suspend fun getApplicationInfoGridItemEntity(id: String): ApplicationInfoGridItemEntity?

    @Delete
    suspend fun deleteApplicationInfoGridItemEntities(entities: List<ApplicationInfoGridItemEntity>)

    @Delete
    suspend fun deleteApplicationInfoGridItemEntity(entity: ApplicationInfoGridItemEntity)
}