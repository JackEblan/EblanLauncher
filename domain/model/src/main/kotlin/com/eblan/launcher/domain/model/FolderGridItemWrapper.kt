package com.eblan.launcher.domain.model

data class FolderGridItemWrapper(
    val folderGridItem: FolderGridItem,
    val applicationInfoFolderGridItems: List<ApplicationInfoFolderGridItem>,
)