package com.eblan.launcher.domain.framework

import java.io.File

interface FileManager {
    val iconsDirectory: File

    suspend fun writeIconBytes(
        iconsDirectory: File,
        name: String,
        icon: ByteArray?,
    ): String?

    suspend fun deleteIcon(name: String)
}