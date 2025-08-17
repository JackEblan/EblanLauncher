package com.eblan.launcher.domain.framework

import java.io.File

interface FileManager {
    suspend fun getDirectory(name: String): File

    suspend fun writeFileBytes(
        directory: File,
        name: String,
        byteArray: ByteArray,
    ): String?

    suspend fun deleteFile(
        directory: File,
        name: String,
    )

    companion object {
        const val ICONS_DIR = "icons"

        const val WIDGETS_DIR = "widgets"

        const val SHORTCUTS_DIR = "shortcuts"
    }
}