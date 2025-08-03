package com.eblan.launcher.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.eblan.launcher.data.room.entity.FolderGridItemWrapperEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderGridItemDao {

    @Transaction
    @Query("SELECT * FROM FolderGridItemEntity")
    fun getFolderGridItemWrapperEntities(): Flow<List<FolderGridItemWrapperEntity>>
}
