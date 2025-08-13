package com.eblan.launcher.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.TextColor

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
data class WidgetGridItemEntity(
    @PrimaryKey
    val id: String,
    val folderId: String?,
    val page: Int,
    val startRow: Int,
    val startColumn: Int,
    val rowSpan: Int,
    val columnSpan: Int,
    val associate: Associate,
    val appWidgetId: Int,
    val packageName: String,
    val componentName: String,
    val configure: String?,
    val minWidth: Int,
    val minHeight: Int,
    val resizeMode: Int,
    val minResizeWidth: Int,
    val minResizeHeight: Int,
    val maxResizeWidth: Int,
    val maxResizeHeight: Int,
    val targetCellHeight: Int,
    val targetCellWidth: Int,
    val preview: String?,
    val override: Boolean,
    val iconSize: Int,
    val textColor: TextColor,
    val textSize: Int,
)