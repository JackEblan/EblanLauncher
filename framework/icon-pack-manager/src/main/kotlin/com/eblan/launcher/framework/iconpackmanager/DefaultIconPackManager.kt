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
    override suspend fun parseAppFilter(iconPackPackageName: String): Map<String, String> {
        return withContext(ioDispatcher) {
            val result = mutableMapOf<String, String>()

            val packContext =
                context.createPackageContext(iconPackPackageName, Context.CONTEXT_IGNORE_SECURITY)

            val resId = packContext.resources.getIdentifier("appfilter", "xml", iconPackPackageName)

            val input = if (resId != 0) {
                packContext.resources.openRawResource(resId)
            } else {
                packContext.assets.open("appfilter.xml")
            }

            input.use { stream ->
                val parser = Xml.newPullParser()
                parser.setInput(stream, null)
                var eventType = parser.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG && parser.name == "item") {
                        val comp = parser.getAttributeValue(null, "component")
                        val drawable = parser.getAttributeValue(null, "drawable")
                        if (!comp.isNullOrBlank() && !drawable.isNullOrBlank()) {
                            result[comp] = drawable
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

            val res = packageContext.resources

            var id = res.getIdentifier(drawableName, "drawable", packageName)

            if (id == 0) {
                id = res.getIdentifier(drawableName, "mipmap", packageName)
            }

            if (id != 0) {
                res.getDrawable(id, packageContext.theme).toByteArray()
            } else {
                null
            }
        }
    }
}