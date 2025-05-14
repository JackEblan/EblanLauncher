package com.eblan.launcher.common.util

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import java.io.ByteArrayOutputStream

fun Drawable.toByteArray(): ByteArray {
    val stream = ByteArrayOutputStream()

    toBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream)

    return stream.toByteArray()
}