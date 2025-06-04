package com.eblan.launcher.data.room.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.eblan.launcher.domain.model.EblanApplicationInfo

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = EblanApplicationInfoEntity::class,
            parentColumns = ["packageName"],
            childColumns = ["applicationInfo_packageName"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class EblanAppWidgetProviderInfoEntity(
    @PrimaryKey val className: String,
    val componentName: String,
    val targetCellWidth: Int,
    val targetCellHeight: Int,
    val minWidth: Int,
    val minHeight: Int,
    val resizeMode: Int,
    val minResizeWidth: Int,
    val minResizeHeight: Int,
    val maxResizeWidth: Int,
    val maxResizeHeight: Int,
    val preview: String?,
    @Embedded(prefix = "applicationInfo_") val eblanApplicationInfo: EblanApplicationInfo,
)