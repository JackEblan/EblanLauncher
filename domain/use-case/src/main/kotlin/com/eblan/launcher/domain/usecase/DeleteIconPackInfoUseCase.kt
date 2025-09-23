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
import com.eblan.launcher.domain.repository.EblanIconPackInfoRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class DeleteIconPackInfoUseCase @Inject constructor(
    private val fileManager: FileManager,
    private val userDataRepository: UserDataRepository,
    private val eblanIconPackInfoRepository: EblanIconPackInfoRepository,
    @Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(iconPackInfoPackageName: String) {
        withContext(ioDispatcher) {
            val eblanIconPackInfo =
                eblanIconPackInfoRepository.getEblanIconPackInfo(packageName = iconPackInfoPackageName)

            if (eblanIconPackInfo != null) {
                eblanIconPackInfoRepository.deleteEblanIconPackInfo(eblanIconPackInfo = eblanIconPackInfo)

                val iconPackInfoPackageName =
                    userDataRepository.userData.first().generalSettings.iconPackInfoPackageName

                val iconPacksDirectory = File(
                    fileManager.getFilesDirectory(name = FileManager.ICON_PACKS_DIR),
                    iconPackInfoPackageName,
                )

                if (iconPacksDirectory.isDirectory && iconPacksDirectory.exists()) {
                    iconPacksDirectory.deleteRecursively()
                }
            }
        }
    }
}
