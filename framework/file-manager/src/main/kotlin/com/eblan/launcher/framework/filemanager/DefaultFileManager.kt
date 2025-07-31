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

internal class DefaultFileManager @Inject constructor(@ApplicationContext private val context: Context) :
    FileManager {
    override val iconsDirectory: File by lazy {
        File(context.filesDir, "icons").apply {
            if (!exists()) mkdirs()
        }
    }

    override val widgetsDirectory: File by lazy {
        File(context.filesDir, "widgets").apply {
            if (!exists()) mkdirs()
        }
    }

    override val shortcutsDirectory: File by lazy {
        File(context.filesDir, "shortcuts").apply {
            if (!exists()) mkdirs()
        }
    }

    override suspend fun writeFileBytes(
        directory: File,
        name: String,
        byteArray: ByteArray,
    ): String? {
        return withContext(Dispatchers.IO) {
            val file = File(directory, name)

            val oldFile = readFileBytes(file = file)

            if (oldFile.contentEquals(byteArray)) {
                file.absolutePath
            } else {
                try {
                    FileOutputStream(file).use { fos ->
                        fos.write(byteArray)
                    }

                    file.absolutePath
                } catch (_: IOException) {
                    null
                }
            }
        }
    }

    override suspend fun deleteFile(directory: File, name: String) {
        withContext(Dispatchers.IO) {
            val file = File(directory, name)

            try {
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun readFileBytes(file: File): ByteArray? {
        return if (file.exists()) {
            try {
                FileInputStream(file).use { fis ->
                    fis.readBytes()
                }
            } catch (_: IOException) {
                null
            }
        } else null
    }
}