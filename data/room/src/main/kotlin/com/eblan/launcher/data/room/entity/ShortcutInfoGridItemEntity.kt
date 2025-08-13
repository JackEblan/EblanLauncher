package com.eblan.launcher.data.room.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItemSettings

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = EblanApplicationInfoEntity::class,
            parentColumns = ["packageName"],
            childColumns = ["packageName"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["packageName"])],
)
data class ShortcutInfoGridItemEntity(
    @PrimaryKey
    val id: String,
    val folderId: String?,
    val page: Int,
    val startRow: Int,
    val startColumn: Int,
    val rowSpan: Int,
    val columnSpan: Int,
    val associate: Associate,
    val shortcutId: String,
    val packageName: String,
    val shortLabel: String,
    val longLabel: String,
    val icon: String?,
    @Embedded val gridItemSettings: GridItemSettings?,
)