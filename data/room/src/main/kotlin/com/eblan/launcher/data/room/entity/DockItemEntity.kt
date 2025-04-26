package com.eblan.launcher.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.eblan.launcher.domain.model.GridItemData

@Entity
data class DockItemEntity(
    @PrimaryKey val id: String,
    val startRow: Int,
    val startColumn: Int,
    val rowSpan: Int,
    val columnSpan: Int,
    val data: GridItemData,
)