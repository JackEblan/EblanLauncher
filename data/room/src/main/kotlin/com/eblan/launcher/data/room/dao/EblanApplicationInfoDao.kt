package com.eblan.launcher.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.eblan.launcher.data.room.entity.EblanApplicationInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EblanApplicationInfoDao {
    @Query("SELECT * FROM EblanApplicationInfoEntity")
    fun getEblanApplicationInfoEntities(): Flow<List<EblanApplicationInfoEntity>>

    @Upsert
    suspend fun upsertEblanApplicationInfoEntities(entities: List<EblanApplicationInfoEntity>)

    @Upsert
    suspend fun upsertEblanApplicationInfoEntity(entity: EblanApplicationInfoEntity)

    @Query("SELECT * FROM EblanApplicationInfoEntity WHERE packageName = :packageName")
    suspend fun getEblanApplicationInfoEntity(packageName: String): EblanApplicationInfoEntity?

    @Query("DELETE FROM EblanApplicationInfoEntity WHERE packageName = :packageName")
    suspend fun deleteEblanApplicationInfoEntityByPackageName(packageName: String)

    @Delete
    suspend fun deleteEblanApplicationInfoEntities(entities: List<EblanApplicationInfoEntity>)
}