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
package com.eblan.launcher.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface EblanAction {
    @Serializable
    data object None : EblanAction

    @Serializable
    data object OpenAppDrawer : EblanAction

    @Serializable
    data object OpenNotificationPanel : EblanAction

    @Serializable
    data class OpenApp(val componentName: String) : EblanAction

    @Serializable
    data object LockScreen : EblanAction

    @Serializable
    data object OpenQuickSettings : EblanAction

    @Serializable
    data object OpenRecents : EblanAction

    companion object {
        const val ACTION = "com.eblan.launcher.EBLAN_ACTION"

        const val NAME = "EblanAction"
    }
}
