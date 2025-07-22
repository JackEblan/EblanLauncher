package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.repository.GridCacheRepository
import javax.inject.Inject

class UpdateShortcutGridItemDataUseCase @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
    private val fileManager: FileManager,
) {
    suspend operator fun invoke(id: Int, shortcutId: String, byteArray: ByteArray?) {
        if (byteArray != null) {
            val icon = fileManager.writeFileBytes(
                directory = fileManager.shortcutsDirectory,
                name = shortcutId,
                byteArray = byteArray,
            )

            gridCacheRepository.updateShortcutGridItemData(id = id, icon = icon)
        }
    }
}