/*
 *
 *   Copyright 2023 Einstein Blanco
 *
 *   Licensed under the GNU General Public License v3.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.gnu.org/licenses/gpl-3.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package com.eblan.launcher.common.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.createBitmap
import java.io.ByteArrayOutputStream

fun Drawable.toByteArray(): ByteArray? {
    if (this is BitmapDrawable) {
        return ByteArrayOutputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

            stream.toByteArray()
        }
    }

    val width = if (!bounds.isEmpty) {
        bounds.width()
    } else {
        intrinsicWidth
    }

    val height = if (!bounds.isEmpty) {
        bounds.height()
    } else {
        intrinsicHeight
    }

    return if (width > 0 && height > 0) {
        val bitmap = createBitmap(
            width = width,
            height = height,
        )

        val canvas = Canvas(bitmap)

        setBounds(0, 0, canvas.width, canvas.height)

        draw(canvas)

        ByteArrayOutputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

            stream.toByteArray()
        }
    } else {
        null
    }
}
