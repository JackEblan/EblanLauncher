package com.eblan.launcher.data.room.entity

import androidx.room.Embedded
import androidx.room.Relation

data class FolderGridItemWrapperEntity(
    @Embedded val folderGridItemEntity: FolderGridItemEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "folderId",
    )
    val applicationInfos: List<ApplicationInfoGridItemEntity>?,

    @Relation(
        parentColumn = "id",
        entityColumn = "folderId",
    )
    val widgets: List<WidgetGridItemEntity>?,

    @Relation(
        parentColumn = "id",
        entityColumn = "folderId",
    )
    val shortcutInfos: List<ShortcutInfoGridItemEntity>?,
)