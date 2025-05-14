package com.eblan.launcher.framework.filemanager

import android.content.Context
import com.eblan.launcher.domain.framework.FileManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

internal class AndroidFileManager @Inject constructor(@ApplicationContext private val context: Context) :
    FileManager {
    override val iconsDirectory: File by lazy {
        File(context.filesDir, "icons").apply {
            if (!exists()) mkdirs()
        }
    }
}