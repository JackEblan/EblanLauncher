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
package com.eblan.launcher.data.repository

import com.eblan.launcher.data.cache.FolderGridCacheDataSource
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.repository.FolderGridCacheRepository
import javax.inject.Inject

internal class DefaultFolderGridCacheRepository @Inject constructor(private val folderGridCacheDataSource: FolderGridCacheDataSource) :
    FolderGridCacheRepository {
    override val gridItemsCache = folderGridCacheDataSource.gridItemsCache

    override fun insertGridItems(gridItems: List<GridItem>) {
        folderGridCacheDataSource.insertGridItems(gridItems = gridItems)
    }

    override fun insertGridItem(gridItem: GridItem) {
        folderGridCacheDataSource.insertGridItem(gridItem = gridItem)
    }

    override suspend fun upsertGridItems(gridItems: List<GridItem>) {
        folderGridCacheDataSource.upsertGridItems(gridItems = gridItems)
    }
}
