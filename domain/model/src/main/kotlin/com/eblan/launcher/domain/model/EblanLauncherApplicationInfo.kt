package com.eblan.launcher.domain.model

data class EblanLauncherApplicationInfo(
    val packageName: String,
    val label: String,
    val icon: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EblanLauncherApplicationInfo

        return packageName == other.packageName
    }

    override fun hashCode(): Int {
        return packageName.hashCode()
    }
}
