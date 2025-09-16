package com.eblan.launcher.domain.framework

interface IconPackManager {
    suspend fun parseAppFilter(iconPackPackageName: String): Map<String, String>

    suspend fun loadByteArrayFromIconPack(
        packageName: String,
        drawableName: String
    ): ByteArray?

    companion object {
        const val ICON_PACK_SERVICE_REQUEST_TYPE = "iconPackServiceRequestType"

        const val ICON_PACK_PACKAGE_NAME = "iconPackPackageName"

        const val ICON_PACK_LABEL = "label"
    }
}