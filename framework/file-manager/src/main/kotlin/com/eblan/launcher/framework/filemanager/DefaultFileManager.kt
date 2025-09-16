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
package com.eblan.launcher.framework.filemanager

import android.content.Context
import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.framework.FileManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

internal class DefaultFileManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : FileManager {
    override suspend fun getFilesDirectory(name: String): File {
        return withContext(ioDispatcher) {
            File(context.filesDir, name).apply {
                if (!exists()) mkdirs()
            }
        }
    }

    override suspend fun getAndUpdateFilePath(
        directory: File,
        name: String,
        byteArray: ByteArray,
    ): String? {
        return withContext(ioDispatcher) {
            val file = File(directory, name)

            val oldFile = readFileBytes(file = file)

            if (oldFile.contentEquals(byteArray)) {
                file.absolutePath
            } else {
                try {
                    FileOutputStream(file).use { fos ->
                        fos.write(byteArray)
                    }

                    file.absolutePath
                } catch (_: IOException) {
                    null
                }
            }
        }
    }

    override suspend fun getFilePath(
        directory: File,
        name: String,
        byteArray: ByteArray,
    ): String? {
        return withContext(ioDispatcher) {
            val file = File(directory, name)

            val oldFile = readFileBytes(file = file)

            if (oldFile.contentEquals(byteArray)) {
                file.absolutePath
            } else {
                null
            }
        }
    }

    private fun readFileBytes(file: File): ByteArray? {
        return if (file.exists()) {
            try {
                FileInputStream(file).use { fis ->
                    fis.readBytes()
                }
            } catch (_: IOException) {
                null
            }
        } else {
            null
        }
    }
}
