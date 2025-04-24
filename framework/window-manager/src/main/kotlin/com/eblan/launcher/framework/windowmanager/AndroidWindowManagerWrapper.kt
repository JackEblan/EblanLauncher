package com.eblan.launcher.framework.windowmanager

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.view.WindowInsets
import android.view.WindowManager
import com.eblan.launcher.domain.model.ScreenSize
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

internal class AndroidWindowManagerWrapper @Inject constructor(@ActivityContext val context: Context) :
    WindowManagerWrapper {
    private val activity = context as Activity

    private val windowManager = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    @Suppress("DEPRECATION")
    override fun getSize(): ScreenSize {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics

            val insets =
                windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())

            val bounds = windowMetrics.bounds

            val width = bounds.width() - insets.left - insets.right

            val height = bounds.height() - insets.top - insets.bottom

            ScreenSize(width = width, height = height)
        } else {
            val size = Point()

            val display = windowManager.defaultDisplay

            display?.getSize(size)

            val width = size.x

            val height = size.y

            ScreenSize(width = width, height = height)
        }
    }
}
