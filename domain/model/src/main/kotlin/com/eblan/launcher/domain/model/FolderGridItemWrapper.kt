package com.eblan.launcher.domain.model

data class FolderGridItemWrapper(
    val folderGridItem: FolderGridItem?,

    val applicationInfos: List<ApplicationInfoGridItem>?,

    val widgets: List<WidgetGridItem>?,

    val shortcutInfos: List<ShortcutInfoGridItem>?,
)