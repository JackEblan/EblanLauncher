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
package com.eblan.launcher.framework.drawable

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.createBitmap
import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

internal class DefaultDrawableWrapper @Inject constructor(
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) : AndroidDrawableWrapper {
    override suspend fun createByteArray(drawable: Drawable): ByteArray? {
        if (drawable is BitmapDrawable) {
            return ByteArrayOutputStream().use { stream ->
                withContext(defaultDispatcher) {
                    drawable.bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

                    stream.toByteArray()
                }
            }
        }

        val width = if (!drawable.bounds.isEmpty) {
            drawable.bounds.width()
        } else {
            drawable.intrinsicWidth
        }

        val height = if (!drawable.bounds.isEmpty) {
            drawable.bounds.height()
        } else {
            drawable.intrinsicHeight
        }

        return if (width > 0 && height > 0) {
            val bitmap = createBitmap(
                width = width,
                height = height,
            )

            val canvas = Canvas(bitmap)

            drawable.setBounds(0, 0, canvas.width, canvas.height)

            drawable.draw(canvas)

            ByteArrayOutputStream().use { stream ->
                withContext(defaultDispatcher) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

                    stream.toByteArray()
                }
            }
        } else {
            null
        }
    }
}
