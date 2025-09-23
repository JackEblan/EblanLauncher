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
package com.eblan.launcher.framework.iconpackmanager

import android.annotation.SuppressLint
import android.content.Context
import android.util.Xml
import com.eblan.launcher.common.util.toByteArray
import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.framework.IconPackManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import javax.inject.Inject

internal class DefaultIconPackManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : IconPackManager {
    @SuppressLint("DiscouragedApi")
    override suspend fun parseAppFilter(iconPackInfoPackageName: String): Map<String, String> {
        return withContext(ioDispatcher) {
            val result = mutableMapOf<String, String>()

            val packageContext =
                context.createPackageContext(iconPackInfoPackageName, Context.CONTEXT_IGNORE_SECURITY)

            packageContext.assets.open("appfilter.xml").use { stream ->
                val parser = Xml.newPullParser()

                parser.setInput(stream, null)

                var eventType = parser.eventType

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG && parser.name == "item") {
                        val component = parser.getAttributeValue(null, "component")

                        val drawable = parser.getAttributeValue(null, "drawable")

                        if (!component.isNullOrBlank() && !drawable.isNullOrBlank()) {
                            result[component] = drawable
                        }
                    }

                    eventType = parser.next()
                }
            }

            result
        }
    }

    @SuppressLint("DiscouragedApi")
    override suspend fun loadByteArrayFromIconPack(
        packageName: String,
        drawableName: String,
    ): ByteArray? {
        return withContext(ioDispatcher) {
            val packageContext =
                context.createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY)

            val resources = packageContext.resources

            val id = resources.getIdentifier(drawableName, "drawable", packageName)

            if (id > 0) {
                resources.getDrawable(id, packageContext.theme).toByteArray()
            } else {
                null
            }
        }
    }
}
