package com.eblan.launcher.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.eblan.launcher.domain.model.GridCell

@Entity
data class GridItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val page: Int, val cells: List<GridCell>,
)
