package com.eblan.launcher.feature.home.model

import android.content.pm.LauncherApps.PinItemRequest
import com.eblan.launcher.domain.model.GridItem

sealed interface GridItemSource {
    val gridItem: GridItem

    data class Existing(override val gridItem: GridItem) : GridItemSource

    data class New(override val gridItem: GridItem) : GridItemSource

    data class Pin(
        override val gridItem: GridItem,
        val pinItemRequest: PinItemRequest,
    ) : GridItemSource
}