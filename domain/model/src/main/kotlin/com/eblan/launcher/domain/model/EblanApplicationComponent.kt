package com.eblan.launcher.domain.model

data class EblanApplicationComponent(
    val eblanApplicationInfos: List<EblanApplicationInfo>,
    val eblanAppWidgetProviderInfos: Map<EblanApplicationInfo, List<EblanAppWidgetProviderInfo>>,
    val eblanShortcutInfos: List<EblanShortcutInfo>,
    val pageCount: Int,
)
