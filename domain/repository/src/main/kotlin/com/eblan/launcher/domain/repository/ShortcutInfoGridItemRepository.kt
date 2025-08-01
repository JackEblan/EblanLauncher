package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.ShortcutInfoGridItem
import kotlinx.coroutines.flow.Flow

interface ShortcutInfoGridItemRepository {
    val shortcutInfoGridItems: Flow<List<GridItem>>

    suspend fun upsertShortcutInfoGridItems(shortcutInfoGridItems: List<ShortcutInfoGridItem>)

    suspend fun upsertShortcutInfoGridItem(shortcutInfoGridItem: ShortcutInfoGridItem): Long

    suspend fun updateShortcutInfoGridItem(shortcutInfoGridItem: ShortcutInfoGridItem)

    suspend fun getShortcutInfoGridItem(id: String): ShortcutInfoGridItem?

    suspend fun deleteShortcutInfoGridItems(shortcutInfoGridItems: List<ShortcutInfoGridItem>)

    suspend fun deleteShortcutInfoGridItem(shortcutInfoGridItem: ShortcutInfoGridItem)
}