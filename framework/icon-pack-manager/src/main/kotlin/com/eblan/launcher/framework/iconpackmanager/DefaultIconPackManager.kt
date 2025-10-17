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
import android.content.res.Resources
import android.content.res.XmlResourceParser
import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.framework.IconPackManager
import com.eblan.launcher.domain.model.IconPackInfoComponent
import com.eblan.launcher.framework.bitmap.AndroidBitmapWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

@SuppressLint("DiscouragedApi")
internal class DefaultIconPackManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val androidBitmapWrapper: AndroidBitmapWrapper,
) : IconPackManager {
    override suspend fun parseAppFilter(packageName: String): List<IconPackInfoComponent> {
        val xmlPullParser = getXmlPullParser(packageName = packageName)

        return try {
            when (xmlPullParser) {
                is XmlResourceParser -> {
                    parseXml(xmlPullParser = xmlPullParser)
                }

                is InputStream -> {
                    val xmlParser = XmlPullParserFactory.newInstance().newPullParser()

                    xmlParser.setInput(xmlPullParser.reader())

                    parseXml(xmlParser)
                }

                else -> {
                    emptyList()
                }
            }
        } finally {
            xmlPullParser?.close()
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
                androidBitmapWrapper.createByteArray(
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

    private fun getXmlPullParser(packageName: String): AutoCloseable? {
        return try {
            val packageContext = context.createPackageContext(
                packageName,
                Context.CONTEXT_IGNORE_SECURITY,
            )

            val res = packageContext.resources

            val xmlId = res.getIdentifier("appfilter", "xml", packageName)

            val rawId = res.getIdentifier("appfilter", "raw", packageName)

            when {
                xmlId != 0 -> {
                    res.getXml(xmlId)
                }

                rawId != 0 -> {
                    res.openRawResource(rawId)
                }

                else -> {
                    packageContext.assets.open("appfilter.xml")
                }
            }
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
            null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun parseXml(xmlPullParser: XmlPullParser): List<IconPackInfoComponent> {
        val iconPackInfoComponents = mutableListOf<IconPackInfoComponent>()

        var eventType = xmlPullParser.eventType

        while (eventType != XmlPullParser.END_DOCUMENT) {
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

        return iconPackInfoComponents
    }
}
