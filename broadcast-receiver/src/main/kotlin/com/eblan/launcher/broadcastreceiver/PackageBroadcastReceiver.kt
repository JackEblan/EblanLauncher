package com.eblan.launcher.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import com.eblan.launcher.domain.common.qualifier.ApplicationScope
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.framework.filemanager.FileManager
import com.eblan.launcher.framework.packagemanager.PackageManagerWrapper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class PackageBroadcastReceiver : BroadcastReceiver() {
    @Inject
    lateinit var eblanApplicationInfoRepository: EblanApplicationInfoRepository

    @Inject
    lateinit var packageManagerWrapper: PackageManagerWrapper

    @Inject
    lateinit var fileManager: FileManager

    @Inject
    @ApplicationScope
    lateinit var appScope: CoroutineScope

    override fun onReceive(context: Context?, intent: Intent?) {
        appScope.launch {
            val eblanApplicationInfos =
                packageManagerWrapper.queryIntentActivities().map { applicationInfo ->
                    val newIcon = withContext(Dispatchers.IO) {
                        val stream = ByteArrayOutputStream()

                        val drawable = packageManagerWrapper.getApplicationIcon(
                            packageName = applicationInfo.packageName,
                        )

                        drawable?.toBitmap()?.compress(Bitmap.CompressFormat.PNG, 100, stream)

                        stream.toByteArray()
                    }

                    val icon = fileManager.writeIconBytes(
                        name = applicationInfo.packageName,
                        newIcon = newIcon,
                    )

                    val label = context?.packageManager?.let(applicationInfo::loadLabel).toString()

                    EblanApplicationInfo(
                        packageName = applicationInfo.packageName,
                        icon = icon,
                        label = label,
                    )
                }

            eblanApplicationInfoRepository.upsertEblanApplicationInfos(eblanApplicationInfos = eblanApplicationInfos)
        }
    }
}