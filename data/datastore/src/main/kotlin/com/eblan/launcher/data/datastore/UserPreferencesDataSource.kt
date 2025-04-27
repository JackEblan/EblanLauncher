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
package com.eblan.launcher.data.datastore

import androidx.datastore.core.DataStore
import com.eblan.launcher.data.datastore.proto.UserPreferences
import com.eblan.launcher.data.datastore.proto.copy
import com.eblan.launcher.domain.model.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<UserPreferences>,
) {
    val userData: Flow<UserData> = dataStore.data.map { userPreferences ->
        UserData(
            rows = userPreferences.rows,
            columns = userPreferences.columns,
            pageCount = userPreferences.pageCount,
            infiniteScroll = userPreferences.infiniteScroll,
            dockRows = userPreferences.dockRows,
            dockColumns = userPreferences.dockColumns,
            dockHeight = userPreferences.dockHeight,
        )
    }

    suspend fun updateRows(rows: Int) {
        dataStore.updateData { userPreferences ->
            userPreferences.copy {
                this.rows = rows
            }
        }
    }

    suspend fun updateColumns(columns: Int) {
        dataStore.updateData { userPreferences ->
            userPreferences.copy {
                this.columns = columns
            }
        }
    }

    suspend fun updatePageCount(pageCount: Int) {
        dataStore.updateData { userPreferences ->
            userPreferences.copy {
                this.pageCount = pageCount
            }
        }
    }

    suspend fun updateInfiniteScroll(infiniteScroll: Boolean) {
        dataStore.updateData { userPreferences ->
            userPreferences.copy {
                this.infiniteScroll = infiniteScroll
            }
        }
    }

    suspend fun updateDockRows(dockRows: Int) {
        dataStore.updateData { userPreferences ->
            userPreferences.copy {
                this.dockRows = dockRows
            }
        }
    }

    suspend fun updateDockColumns(dockColumns: Int) {
        dataStore.updateData { userPreferences ->
            userPreferences.copy {
                this.dockColumns = dockColumns
            }
        }
    }

    suspend fun updateDockHeight(dockHeight: Int) {
        dataStore.updateData { userPreferences ->
            userPreferences.copy {
                this.dockHeight = dockHeight
            }
        }
    }
}