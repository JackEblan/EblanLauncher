package com.eblan.launcher.framework.filemanager

interface FileManager {
    suspend fun writeIconBytes(name: String, newIcon: ByteArray?): String?
}