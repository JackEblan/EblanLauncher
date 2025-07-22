package com.eblan.launcher.common.util

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

suspend fun Drawable.toByteArray(): ByteArray {
    return withContext(Dispatchers.Default){
        val stream = ByteArrayOutputStream()

        toBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream)

        stream.toByteArray()
    }
}