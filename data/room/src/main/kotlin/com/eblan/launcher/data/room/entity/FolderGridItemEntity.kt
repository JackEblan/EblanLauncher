package com.eblan.launcher.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.TextColor

@Entity
data class FolderGridItemEntity(
    @PrimaryKey
    val id: String,
    val folderId: String?,
    val page: Int,
    val startRow: Int,
    val startColumn: Int,
    val rowSpan: Int,
    val columnSpan: Int,
    val associate: Associate,
    val label: String,
    val override: Boolean,
    val iconSize: Int,
    val textColor: TextColor,
    val textSize: Int,
)