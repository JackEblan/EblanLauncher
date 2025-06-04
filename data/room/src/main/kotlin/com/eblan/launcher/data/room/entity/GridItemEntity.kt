package com.eblan.launcher.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItemData

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = EblanApplicationInfoEntity::class,
            parentColumns = ["packageName"],
            childColumns = ["dataId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["dataId"])],
)
data class GridItemEntity(
    @PrimaryKey val id: String,
    val page: Int,
    val startRow: Int,
    val startColumn: Int,
    val rowSpan: Int,
    val columnSpan: Int,
    val dataId: String,
    val data: GridItemData,
    val associate: Associate,
)