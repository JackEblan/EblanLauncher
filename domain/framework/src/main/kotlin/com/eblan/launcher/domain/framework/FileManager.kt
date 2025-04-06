package com.eblan.launcher.domain.framework

interface FileManager {
    suspend fun writeIconBytes(name: String, icon: ByteArray?): String?
}