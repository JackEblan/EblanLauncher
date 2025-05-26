package com.eblan.launcher.framework.filemanager

import android.content.Context
import com.eblan.launcher.domain.framework.FileManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

internal class AndroidFileManager @Inject constructor(@ApplicationContext private val context: Context) :
    FileManager {
    override val iconsDirectory: File by lazy {
        File(context.filesDir, "icons").apply {
            if (!exists()) mkdirs()
        }
    }

    override suspend fun writeIconBytes(
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

    override suspend fun deleteIcon(name: String) {
        withContext(Dispatchers.IO) {
            val iconFile = File(iconsDirectory, name)

            try {
                if (iconFile.exists()) {
                    iconFile.delete()
                }
            } catch (e: IOException) {
                e.printStackTrace()
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