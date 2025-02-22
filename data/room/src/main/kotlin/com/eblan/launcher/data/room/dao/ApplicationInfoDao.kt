package com.eblan.launcher.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.eblan.launcher.data.room.entity.EblanLauncherApplicationInfoEntity

@Dao
interface ApplicationInfoDao {
    @Query("SELECT * FROM EblanLauncherApplicationInfoEntity WHERE gridItemId = :gridItemId")
    suspend fun getApplicationInfoEntity(gridItemId: Int): EblanLauncherApplicationInfoEntity

    @Upsert
    suspend fun upsertApplicationInfoEntity(applicationInfoEntity: EblanLauncherApplicationInfoEntity)
}