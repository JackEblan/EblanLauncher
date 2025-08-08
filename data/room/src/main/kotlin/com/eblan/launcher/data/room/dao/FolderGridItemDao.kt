package com.eblan.launcher.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.eblan.launcher.data.room.entity.FolderGridItemEntity
import com.eblan.launcher.data.room.entity.FolderGridItemWrapperEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderGridItemDao {

    @Transaction
    @Query("SELECT * FROM FolderGridItemEntity")
    fun getFolderGridItemWrapperEntities(): Flow<List<FolderGridItemWrapperEntity>>

    @Transaction
    @Query("SELECT * FROM FolderGridItemEntity WHERE id = :id")
    suspend fun getFolderGridItemWrapperEntity(id: String): FolderGridItemWrapperEntity?

    @Upsert
    suspend fun upsertFolderGridItemEntities(entities: List<FolderGridItemEntity>)

    @Upsert
    suspend fun upsertFolderGridItemEntity(entity: FolderGridItemEntity): Long

    @Update
    suspend fun updateFolderGridItemEntity(entity: FolderGridItemEntity)

    @Delete
    suspend fun deleteFolderGridItemEntity(entity: FolderGridItemEntity)
}