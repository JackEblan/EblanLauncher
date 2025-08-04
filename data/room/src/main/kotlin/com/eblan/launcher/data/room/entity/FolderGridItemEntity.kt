package com.eblan.launcher.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.eblan.launcher.domain.model.Associate

@Entity
data class FolderGridItemEntity(
    @PrimaryKey
    val id: String,
    val page: Int,
    val startRow: Int,
    val startColumn: Int,
    val rowSpan: Int,
    val columnSpan: Int,
    val associate: Associate,
    val label: String,
)
