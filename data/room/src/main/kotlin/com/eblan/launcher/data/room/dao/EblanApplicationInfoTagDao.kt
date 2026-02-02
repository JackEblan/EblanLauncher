package com.eblan.launcher.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.eblan.launcher.data.room.entity.EblanApplicationInfoTagEntity

@Dao
interface EblanApplicationInfoTagDao {
    @Insert
    suspend fun insertTag(tag: EblanApplicationInfoTagEntity): Long

    @Query("SELECT * FROM EblanApplicationInfoTagEntity ORDER BY name")
    suspend fun getEblanApplicationInfoTagEntities(): List<EblanApplicationInfoTagEntity>

    @Update
    suspend fun updateEblanApplicationInfoTagEntity(tag: EblanApplicationInfoTagEntity)

    @Delete
    suspend fun deleteEblanApplicationInfoTagEntity(tag: EblanApplicationInfoTagEntity)
}