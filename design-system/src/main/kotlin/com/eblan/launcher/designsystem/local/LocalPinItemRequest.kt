package com.eblan.launcher.designsystem.local

import androidx.compose.runtime.staticCompositionLocalOf
import com.eblan.launcher.framework.launcherapps.PinItemRequestWrapper

val LocalPinItemRequest = staticCompositionLocalOf<PinItemRequestWrapper> {
    error("No PinItemRequest provided")
}
