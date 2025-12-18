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
package com.eblan.launcher.domain.usecase.applicationcomponent

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.model.EblanApplicationInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class RestoreEblanApplicationInfoUseCase @Inject constructor(
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(eblanApplicationInfo: EblanApplicationInfo): EblanApplicationInfo {
        return withContext(ioDispatcher) {
            eblanApplicationInfo.customIcon?.let { customIcon ->
                val customIconFile = File(customIcon)

                if (customIconFile.exists()) {
                    File(customIcon).delete()
                }
            }

            eblanApplicationInfo.copy(
                customIcon = null,
                customLabel = null,
            )
        }
    }
}
