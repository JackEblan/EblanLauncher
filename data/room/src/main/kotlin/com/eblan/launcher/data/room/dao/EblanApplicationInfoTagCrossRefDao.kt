package com.eblan.launcher.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.eblan.launcher.data.room.entity.EblanApplicationInfoTagCrossRefEntity

@Dao
interface EblanApplicationInfoTagCrossRefDao {

    // CREATE (link tag ↔ app)
    @Insert
    suspend fun insertEblanApplicationInfoTagCrossRefEntity(eblanApplicationInfoTagCrossRefEntity: EblanApplicationInfoTagCrossRefEntity)

    // DELETE (unlink one tag from one app)
    @Query(
        """
        DELETE FROM EblanApplicationInfoTagCrossRefEntity
        WHERE componentName = :componentName
          AND serialNumber = :serialNumber
          AND id = :tagId
    """,
    )
    suspend fun deleteEblanApplicationInfoTagCrossRefEntity(
        componentName: String,
        serialNumber: Long,
        tagId: Long,
    )
}