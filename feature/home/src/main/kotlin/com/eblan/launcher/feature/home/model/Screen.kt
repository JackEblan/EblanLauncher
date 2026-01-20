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
package com.eblan.launcher.feature.home.model

import com.eblan.launcher.domain.model.FolderDataById

internal sealed interface Screen {
    data object Pager : Screen

    data object Drag : Screen

    data object Resize : Screen

    data object Loading : Screen

    data object EditPage : Screen

    data class Folder(val folderDataById: FolderDataById) : Screen

    data class FolderDrag(val folderDataById: FolderDataById) : Screen
}
