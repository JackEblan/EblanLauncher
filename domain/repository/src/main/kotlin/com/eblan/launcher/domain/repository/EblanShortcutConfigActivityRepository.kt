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

import com.eblan.launcher.domain.model.EblanShortcutConfigActivity
import kotlinx.coroutines.flow.Flow

interface EblanShortcutConfigActivityRepository {
    val eblanShortcutConfigActivities: Flow<List<EblanShortcutConfigActivity>>

    suspend fun upsertEblanShortcutConfigActivities(eblanShortcutConfigActivities: List<EblanShortcutConfigActivity>)

    suspend fun upsertEblanShortcutConfigActivity(eblanShortcutConfigActivity: EblanShortcutConfigActivity)

    suspend fun getEblanShortcutConfigActivity(packageName: String): List<EblanShortcutConfigActivity>

    suspend fun deleteEblanShortcutConfigActivity(
        serialNumber: Long,
        packageName: String,
    )

    suspend fun deleteEblanShortcutConfigActivities(eblanShortcutConfigActivities: List<EblanShortcutConfigActivity>)

    suspend fun getEblanShortcutConfigActivity(
        serialNumber: Long,
        packageName: String,
    ): List<EblanShortcutConfigActivity>
}
