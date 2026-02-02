package com.eblan.launcher.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.eblan.launcher.data.room.entity.EblanApplicationInfoTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EblanApplicationInfoTagDao {
    @Query("SELECT * FROM EblanApplicationInfoTagEntity ORDER BY name")
    fun getEblanApplicationInfoTagEntities(): Flow<List<EblanApplicationInfoTagEntity>>

    @Insert
    suspend fun insertEblanApplicationInfoTagEntity(entity: EblanApplicationInfoTagEntity)

    @Update
    suspend fun updateEblanApplicationInfoTagEntity(entity: EblanApplicationInfoTagEntity)

    @Delete
    suspend fun deleteEblanApplicationInfoTagEntity(entity: EblanApplicationInfoTagEntity)
}