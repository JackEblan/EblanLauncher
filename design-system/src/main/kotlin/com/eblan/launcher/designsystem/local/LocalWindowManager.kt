package com.eblan.launcher.designsystem.local

import androidx.compose.runtime.staticCompositionLocalOf
import com.eblan.launcher.framework.windowmanager.WindowManagerWrapper

val LocalWindowManager = staticCompositionLocalOf<WindowManagerWrapper> {
    error("No WindowManager provided")
}
