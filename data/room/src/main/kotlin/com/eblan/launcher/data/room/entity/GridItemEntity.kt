package com.eblan.launcher.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.eblan.launcher.domain.model.GridCell
import com.eblan.launcher.domain.model.GridItemData

@Entity
data class GridItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val page: Int, val cells: List<GridCell>,
    val data: GridItemData?,
)