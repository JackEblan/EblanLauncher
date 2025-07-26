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
import com.eblan.launcher.data.datastore.proto.AppDrawerSettingsProto
import com.eblan.launcher.data.datastore.proto.HomeSettingsProto
import com.eblan.launcher.data.datastore.proto.TextColorProto
import com.eblan.launcher.data.datastore.proto.UserData.UserDataProto
import com.eblan.launcher.data.datastore.proto.copy
import com.eblan.launcher.data.datastore.proto.homeSettingsProto
import com.eblan.launcher.domain.model.AppDrawerSettings
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.domain.model.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserDataStore @Inject constructor(private val dataStore: DataStore<UserDataProto>) {
    val userData: Flow<UserData> = dataStore.data.map { userDataProto ->
        UserData(
            homeSettings = userDataProto
                .homeSettingsProto
                .toHomeSettings(),
            appDrawerSettings = userDataProto
                .appDrawerSettingsProto
                .toAppDrawerSettings(),
        )
    }

    suspend fun updateRows(rows: Int) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                homeSettingsProto {
                    this.rows = rows
                }
            }
        }
    }

    suspend fun updateColumns(columns: Int) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                homeSettingsProto {
                    this.columns = columns
                }
            }
        }
    }

    suspend fun updatePageCount(pageCount: Int) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                homeSettingsProto {
                    this.pageCount = pageCount
                }
            }
        }
    }

    suspend fun updateInfiniteScroll(infiniteScroll: Boolean) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                homeSettingsProto {
                    this.infiniteScroll = infiniteScroll
                }
            }
        }
    }

    suspend fun updateDockRows(dockRows: Int) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                homeSettingsProto {
                    this.dockRows = dockRows
                }
            }
        }
    }

    suspend fun updateDockColumns(dockColumns: Int) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                homeSettingsProto {
                    this.dockColumns = dockColumns
                }
            }
        }
    }

    suspend fun updateDockHeight(dockHeight: Int) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                homeSettingsProto {
                    this.dockHeight = dockHeight
                }
            }
        }
    }

    suspend fun updateInitialPage(initialPage: Int) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                homeSettingsProto {
                    this.initialPage = initialPage
                }
            }
        }
    }

    private fun HomeSettingsProto.toHomeSettings(): HomeSettings {
        return HomeSettings(
            rows = rows,
            columns = columns,
            pageCount = pageCount,
            infiniteScroll = infiniteScroll,
            dockRows = dockRows,
            dockColumns = dockColumns,
            dockHeight = dockHeight,
            textColor = textColorProto.toTextColor(),
            initialPage = initialPage,
        )
    }

    private fun AppDrawerSettingsProto.toAppDrawerSettings(): AppDrawerSettings {
        return AppDrawerSettings(
            appDrawerColumns = appDrawerColumns,
            appDrawerRowsHeight = appDrawerRowsHeight,
        )
    }

    private fun TextColorProto.toTextColor(): TextColor {
        return when (this) {
            TextColorProto.System, TextColorProto.UNRECOGNIZED -> TextColor.System
            TextColorProto.Light -> TextColor.Light
            TextColorProto.Dark -> TextColor.Dark
        }
    }
}