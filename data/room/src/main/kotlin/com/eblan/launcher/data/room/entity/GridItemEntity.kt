package com.eblan.launcher.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class GridItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val page: Int,
    val startRow: Int,
    val startColumn: Int,
    val rowSpan: Int,
    val columnSpan: Int,
)