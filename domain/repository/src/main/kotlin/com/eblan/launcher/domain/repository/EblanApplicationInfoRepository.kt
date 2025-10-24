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
package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.EblanApplicationInfo
import kotlinx.coroutines.flow.Flow

interface EblanApplicationInfoRepository {
    val eblanApplicationInfos: Flow<List<EblanApplicationInfo>>

    suspend fun upsertEblanApplicationInfos(eblanApplicationInfos: List<EblanApplicationInfo>)

    suspend fun upsertEblanApplicationInfo(eblanApplicationInfo: EblanApplicationInfo)

    suspend fun getEblanApplicationInfo(packageName: String): EblanApplicationInfo?

    suspend fun deleteEblanApplicationInfo(
        serialNumber: Long,
        packageName: String,
    )

    suspend fun deleteEblanApplicationInfos(eblanApplicationInfos: List<EblanApplicationInfo>)

    suspend fun getEblanApplicationInfo(
        serialNumber: Long,
        packageName: String,
    ): EblanApplicationInfo?
}
