package com.eblan.launcher.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    primaryKeys = ["componentName", "serialNumber", "id"],
    foreignKeys = [
        ForeignKey(
            entity = EblanApplicationInfoEntity::class,
            parentColumns = ["componentName", "serialNumber"],
            childColumns = ["componentName", "serialNumber"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = EblanApplicationInfoTagEntity::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("id"),
        Index(value = ["componentName", "serialNumber"]),
    ],
)
data class EblanApplicationInfoTagCrossRefEntity(
    val componentName: String,
    val serialNumber: Long,
    val id: Long,
)
