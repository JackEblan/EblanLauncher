package com.eblan.launcher.domain.framework

import java.io.File

interface FileManager {
    val iconsDirectory: File

    val previewsDirectory: File

    suspend fun writeFileBytes(
        directory: File,
        name: String,
        byteArray: ByteArray,
    ): String?

    suspend fun deleteFile(name: String)
}