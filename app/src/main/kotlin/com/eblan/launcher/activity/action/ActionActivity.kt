package com.eblan.launcher.activity.action

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.eblan.launcher.R
import com.eblan.launcher.activity.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.graphics.createBitmap

@AndroidEntryPoint
class ActionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onConfirmShortcut()
    }

    private fun onConfirmShortcut() {
        // TODO This is just a sample code
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

        val result = Intent().apply {
            putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
            putExtra(Intent.EXTRA_SHORTCUT_NAME, "My Action Shortcut")
            putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap)
        }

        setResult(RESULT_OK, result)
        finish()
    }
}