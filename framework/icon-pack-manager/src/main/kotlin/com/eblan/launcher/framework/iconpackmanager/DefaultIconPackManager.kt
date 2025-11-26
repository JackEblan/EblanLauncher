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
import android.content.res.XmlResourceParser
import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.framework.IconPackManager
import com.eblan.launcher.domain.model.IconPackInfoComponent
import com.eblan.launcher.framework.bytearray.AndroidByteArrayWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import javax.inject.Inject

@SuppressLint("DiscouragedApi")
internal class DefaultIconPackManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val androidByteArrayWrapper: AndroidByteArrayWrapper,
) : IconPackManager {
    override suspend fun parseAppFilter(packageName: String): List<IconPackInfoComponent> {
        return withContext(ioDispatcher) {
            try {
                val packageContext = context.createPackageContext(
                    packageName,
                    Context.CONTEXT_IGNORE_SECURITY,
                )

                val resources = packageContext.resources

                val xmlId = resources.getIdentifier("appfilter", "xml", packageName)

                val rawId = resources.getIdentifier("appfilter", "raw", packageName)

                val input = when {
                    xmlId != 0 -> {
                        resources.getXml(xmlId)
                    }

                    rawId != 0 -> {
                        resources.openRawResource(rawId)
                    }

                    else -> {
                        packageContext.assets.open("appfilter.xml")
                    }
                }

                input.use { source ->
                    when (source) {
                        is XmlResourceParser -> {
                            parseXml(xmlPullParser = source)
                        }

                        is InputStream -> {
                            val xmlPullParser = XmlPullParserFactory.newInstance().newPullParser()

                            xmlPullParser.setInput(source.reader())

                            parseXml(xmlPullParser = xmlPullParser)
                        }

                        else -> {
                            emptyList()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

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
                androidByteArrayWrapper.createByteArray(
                    drawable = resources.getDrawable(
                        id,
                        packageContext.theme,
                    ),
                )
            } else {
                null
            }
        }
    }

    private suspend fun parseXml(xmlPullParser: XmlPullParser): List<IconPackInfoComponent> {
        val iconPackInfoComponents = mutableListOf<IconPackInfoComponent>()

        var eventType = xmlPullParser.eventType

        withContext(ioDispatcher) {
            while (isActive && eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && xmlPullParser.name == "item") {
                    val component = xmlPullParser.getAttributeValue(null, "component")

                    val drawable = xmlPullParser.getAttributeValue(null, "drawable")

                    if (!component.isNullOrBlank() && !drawable.isNullOrBlank()) {
                        iconPackInfoComponents.add(
                            IconPackInfoComponent(
                                component = component,
                                drawable = drawable,
                            ),
                        )
                    }
                }

                eventType = xmlPullParser.next()
            }
        }

        return iconPackInfoComponents
    }
}
