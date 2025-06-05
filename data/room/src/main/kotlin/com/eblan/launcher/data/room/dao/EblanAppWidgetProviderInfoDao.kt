package com.eblan.launcher.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.eblan.launcher.data.room.entity.EblanAppWidgetProviderInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EblanAppWidgetProviderInfoDao {

    @Query("SELECT * FROM EblanAppWidgetProviderInfoEntity")
    fun getEblanAppWidgetProviderInfos(): Flow<List<EblanAppWidgetProviderInfoEntity>>

    @Upsert
    suspend fun upsertEblanAppWidgetProviderInfoEntities(entities: List<EblanAppWidgetProviderInfoEntity>)

    @Upsert
    suspend fun upsertEblanAppWidgetProviderInfoEntity(entity: EblanAppWidgetProviderInfoEntity)

    @Delete
    suspend fun deleteEblanAppWidgetProviderInfoEntities(entities: List<EblanAppWidgetProviderInfoEntity>)
}