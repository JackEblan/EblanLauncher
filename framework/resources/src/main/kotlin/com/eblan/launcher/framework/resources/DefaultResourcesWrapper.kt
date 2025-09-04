package com.eblan.launcher.framework.resources

import android.content.Context
import android.content.res.Configuration
import com.eblan.launcher.domain.framework.ResourcesWrapper
import com.eblan.launcher.domain.model.DarkThemeConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class DefaultResourcesWrapper @Inject constructor(@ApplicationContext private val context: Context) :
    ResourcesWrapper {
    override fun getSystemTheme(): DarkThemeConfig {
        return when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> DarkThemeConfig.Dark
            Configuration.UI_MODE_NIGHT_NO -> DarkThemeConfig.Light
            else -> throw IllegalStateException("Unknown UI Mode")
        }
    }
}