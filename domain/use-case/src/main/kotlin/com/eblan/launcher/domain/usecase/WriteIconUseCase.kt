package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

class WriteIconUseCase @Inject constructor(
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val packageManagerWrapper: PackageManagerWrapper,
    private val fileManager: FileManager,
) {
    suspend operator fun invoke() {
        val eblanApplicationInfos =
            packageManagerWrapper.queryIntentActivities().map { packageManagerApplicationInfo ->
                val icon = writeIconBytes(
                    iconsDirectory = fileManager.iconsDirectory,
                    name = packageManagerApplicationInfo.packageName,
                    icon = packageManagerApplicationInfo.icon,
                )

                EblanApplicationInfo(
                    packageName = packageManagerApplicationInfo.packageName,
                    icon = icon,
                    label = packageManagerApplicationInfo.label,
                )
            }

        eblanApplicationInfoRepository.upsertEblanApplicationInfos(eblanApplicationInfos = eblanApplicationInfos)
    }

    private suspend fun writeIconBytes(
        iconsDirectory: File,
        name: String,
        icon: ByteArray?,
    ): String? {
        return withContext(Dispatchers.IO) {
            val iconFile = File(iconsDirectory, name)

            val oldIcon = readIconBytes(iconFile = iconFile)

            if (oldIcon.contentEquals(icon)) {
                iconFile.absolutePath
            } else {
                try {
                    FileOutputStream(iconFile).use { fos ->
                        fos.write(icon)
                    }

                    iconFile.absolutePath
                } catch (_: IOException) {
                    null
                }
            }
        }
    }

    private fun readIconBytes(iconFile: File): ByteArray? {
        return if (iconFile.exists()) {
            try {
                FileInputStream(iconFile).use { fis ->
                    fis.readBytes()
                }
            } catch (_: IOException) {
                null
            }
        } else null
    }
}