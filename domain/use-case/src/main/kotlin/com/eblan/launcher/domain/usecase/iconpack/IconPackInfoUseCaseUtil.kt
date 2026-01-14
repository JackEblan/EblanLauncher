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
package com.eblan.launcher.domain.usecase.iconpack

import com.eblan.launcher.domain.framework.IconPackManager
import com.eblan.launcher.domain.model.IconPackInfoComponent
import java.io.File

internal suspend fun cacheIconPackFile(
    iconPackManager: IconPackManager,
    appFilter: List<IconPackInfoComponent>,
    iconPackInfoPackageName: String,
    iconPackInfoDirectory: File,
    componentName: String,
    packageName: String,
) {
    appFilter.find { iconPackInfoComponent ->
        iconPackInfoComponent.component.contains(componentName) ||
            iconPackInfoComponent.component.contains(packageName)
    }?.let { iconPackInfoComponent ->
        iconPackManager.createIconPackInfoPath(
            packageName = iconPackInfoPackageName,
            iconPackInfoComponent = iconPackInfoComponent,
            iconPackInfoDirectory = iconPackInfoDirectory,
        )
    }
}
