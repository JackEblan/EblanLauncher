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
package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.framework.IconPackManager
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class UpdateIconPackInfoUseCase @Inject constructor(
    private val fileManager: FileManager,
    private val iconPackManager: IconPackManager,
    private val userDataRepository: UserDataRepository,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(packageName: String) {
        withContext(defaultDispatcher) {
            val iconPackInfoPackageName =
                userDataRepository.userData.first().generalSettings.iconPackInfoPackageName

            if (iconPackInfoPackageName.isNotEmpty()) {
                val iconPackDirectory = File(
                    fileManager.getFilesDirectory(name = FileManager.ICON_PACKS_DIR),
                    iconPackInfoPackageName,
                ).apply { if (!exists()) mkdirs() }

                val appFilter =
                    iconPackManager.parseAppFilter(packageName = iconPackInfoPackageName)

                val iconPackInfoComponent = appFilter.find { iconPackInfoComponent ->
                    iconPackInfoComponent.component.contains(packageName)
                } ?: return@withContext

                val byteArray = iconPackManager.loadByteArrayFromIconPack(
                    packageName = iconPackInfoPackageName,
                    drawableName = iconPackInfoComponent.drawable,
                ) ?: return@withContext

                fileManager.getAndUpdateFilePath(
                    directory = iconPackDirectory,
                    name = packageName,
                    byteArray = byteArray,
                )
            }
        }
    }
}
