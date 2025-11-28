package com.eblan.launcher.activity.action

import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Canvas
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.eblan.launcher.R
import com.eblan.launcher.activity.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.graphics.createBitmap
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import javax.inject.Inject

@AndroidEntryPoint
class ActionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createShortcutResult()
    }

    private fun createShortcutResult() {
        val bitmap = ContextCompat.getDrawable(
            this,
            R.drawable.outline_apps_24,
        )?.let { drawable ->
            createBitmap(
                width = drawable.intrinsicWidth,
                height = drawable.intrinsicHeight
            ).also { bitmap ->
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
            }
        }

        val shortcutIntent = Intent(this, MainActivity::class.java).apply {
            action = "com.eblan.launcher.ACTION_SHORTCUT"
        }

        val legacyExtras = Intent().apply {
            putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
            putExtra(Intent.EXTRA_SHORTCUT_NAME, "My Action Shortcut")
            putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap)
        }

        val resultIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = getSystemService(ShortcutManager::class.java)

            if (shortcutManager.isRequestPinShortcutSupported) {
                val shortcut = ShortcutInfo.Builder(this, "my_shortcut_id")
                    .setShortLabel("My Shortcut")
                    .setIcon(Icon.createWithResource(this, R.drawable.outline_apps_24))
                    .setIntent(shortcutIntent)
                    .build()

                shortcutManager.createShortcutResultIntent(shortcut)?.apply {
                    putExtras(legacyExtras)
                }
            } else {
                legacyExtras
            }
        } else {
            legacyExtras
        }

        setResult(RESULT_OK, resultIntent)

        finish()
    }
}