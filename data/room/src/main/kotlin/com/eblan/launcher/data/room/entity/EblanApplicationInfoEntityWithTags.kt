package com.eblan.launcher.data.room.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class EblanApplicationInfoEntityWithTags(
    @Embedded val eblanApplicationInfoEntity: EblanApplicationInfoEntity,
    @Relation(
        parentColumn = "componentName",
        entityColumn = "id",
        associateBy = Junction(
            value = EblanApplicationInfoTagCrossRefEntity::class,
            parentColumn = "componentName",
            entityColumn = "id",
        ),
    )
    val eblanApplicationInfoTagEntities: List<EblanApplicationInfoTagEntity>,
)