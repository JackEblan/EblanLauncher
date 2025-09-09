package com.eblan.launcher.ui.local

import androidx.compose.runtime.staticCompositionLocalOf
import com.eblan.launcher.domain.framework.FileManager

val LocalFileManager = staticCompositionLocalOf<FileManager> {
    error("No FileManager provided")
}