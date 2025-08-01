package com.eblan.launcher.domain.model

data class EblanShortcutInfo(
    val shortcutId: String,
    val packageName: String,
    val shortLabel: String,
    val longLabel: String,
    val eblanApplicationInfo: EblanApplicationInfo,
    val icon: String?,
)
