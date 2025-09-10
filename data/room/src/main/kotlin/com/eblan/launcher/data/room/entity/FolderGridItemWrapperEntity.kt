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
package com.eblan.launcher.data.room.entity

import androidx.room.Embedded
import androidx.room.Relation

data class FolderGridItemWrapperEntity(
    @Embedded val folderGridItemEntity: FolderGridItemEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "folderId",
    )
    val applicationInfos: List<ApplicationInfoGridItemEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "folderId",
    )
    val widgets: List<WidgetGridItemEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "folderId",
    )
    val shortcutInfos: List<ShortcutInfoGridItemEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "folderId",
    )
    val folders: List<FolderGridItemEntity>,
)
