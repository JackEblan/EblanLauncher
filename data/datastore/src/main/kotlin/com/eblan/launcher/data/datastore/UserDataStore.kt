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
import com.eblan.launcher.data.datastore.mapper.toAppDrawerSettings
import com.eblan.launcher.data.datastore.mapper.toDarkThemeConfigProto
import com.eblan.launcher.data.datastore.mapper.toGeneralSettings
import com.eblan.launcher.data.datastore.mapper.toGestureActionProto
import com.eblan.launcher.data.datastore.mapper.toGestureSettings
import com.eblan.launcher.data.datastore.mapper.toHomeSettings
import com.eblan.launcher.data.datastore.mapper.toTextColorProto
import com.eblan.launcher.data.datastore.mapper.toThemeBrandProto
import com.eblan.launcher.data.datastore.proto.UserDataProto
import com.eblan.launcher.data.datastore.proto.appdrawer.copy
import com.eblan.launcher.data.datastore.proto.copy
import com.eblan.launcher.data.datastore.proto.general.copy
import com.eblan.launcher.data.datastore.proto.gesture.copy
import com.eblan.launcher.data.datastore.proto.home.copy
import com.eblan.launcher.domain.model.DarkThemeConfig
import com.eblan.launcher.domain.model.GestureAction
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.domain.model.ThemeBrand
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
            gestureSettings = userDataProto.gestureSettingsProto.toGestureSettings(),
            generalSettings = userDataProto.generalSettingsProto.toGeneralSettings(),
        )
    }

    suspend fun updateRows(rows: Int) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                homeSettingsProto = userDataProto.homeSettingsProto.copy {
                    this.rows = rows
                }
            }
        }
    }

    suspend fun updateColumns(columns: Int) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                homeSettingsProto = userDataProto.homeSettingsProto.copy {
                    this.columns = columns
                }
            }
        }
    }

    suspend fun updatePageCount(pageCount: Int) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                homeSettingsProto = userDataProto.homeSettingsProto.copy {
                    this.pageCount = pageCount
                }
            }
        }
    }

    suspend fun updateInfiniteScroll(infiniteScroll: Boolean) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                homeSettingsProto = userDataProto.homeSettingsProto.copy {
                    this.infiniteScroll = infiniteScroll
                }
            }
        }
    }

    suspend fun updateDockRows(dockRows: Int) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                homeSettingsProto = userDataProto.homeSettingsProto.copy {
                    this.dockRows = dockRows
                }
            }
        }
    }

    suspend fun updateDockColumns(dockColumns: Int) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                homeSettingsProto = userDataProto.homeSettingsProto.copy {
                    this.dockColumns = dockColumns
                }
            }
        }
    }

    suspend fun updateDockHeight(dockHeight: Int) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                homeSettingsProto = userDataProto.homeSettingsProto.copy {
                    this.dockHeight = dockHeight
                }
            }
        }
    }

    suspend fun updateInitialPage(initialPage: Int) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                homeSettingsProto = userDataProto.homeSettingsProto.copy {
                    this.initialPage = initialPage
                }
            }
        }
    }

    suspend fun updateDoubleTap(gestureAction: GestureAction) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                gestureSettingsProto = userDataProto.gestureSettingsProto.copy {
                    this.doubleTapProto = gestureAction.toGestureActionProto()
                }
            }
        }
    }

    suspend fun updateSwipeUp(gestureAction: GestureAction) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                gestureSettingsProto = userDataProto.gestureSettingsProto.copy {
                    this.swipeUpProto = gestureAction.toGestureActionProto()
                }
            }
        }
    }

    suspend fun updateSwipeDown(gestureAction: GestureAction) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                gestureSettingsProto = userDataProto.gestureSettingsProto.copy {
                    this.swipeDownProto = gestureAction.toGestureActionProto()
                }
            }
        }
    }

    suspend fun updateTextColor(textColor: TextColor) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                homeSettingsProto = userDataProto.homeSettingsProto.copy {
                    this.gridItemSettingsProto = this.gridItemSettingsProto.copy {
                        this.textColorProto = textColor.toTextColorProto()
                    }
                }
            }
        }
    }

    suspend fun updateWallpaperScroll(wallpaperScroll: Boolean) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                homeSettingsProto = userDataProto.homeSettingsProto.copy {
                    this.wallpaperScroll = wallpaperScroll
                }
            }
        }
    }

    suspend fun updateFolderRows(folderRows: Int) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                homeSettingsProto = userDataProto.homeSettingsProto.copy {
                    this.folderRows = folderRows
                }
            }
        }
    }

    suspend fun updateFolderColumns(folderColumns: Int) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                homeSettingsProto = userDataProto.homeSettingsProto.copy {
                    this.folderColumns = folderColumns
                }
            }
        }
    }

    suspend fun updateAppDrawerColumns(appDrawerColumns: Int) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                appDrawerSettingsProto = userDataProto.appDrawerSettingsProto.copy {
                    this.appDrawerColumns = appDrawerColumns
                }
            }
        }
    }

    suspend fun updateAppDrawerRowsHeight(appDrawerRowsHeight: Int) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                appDrawerSettingsProto = userDataProto.appDrawerSettingsProto.copy {
                    this.appDrawerRowsHeight = appDrawerRowsHeight
                }
            }
        }
    }

    suspend fun updateIconSize(iconSize: Int) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                homeSettingsProto = userDataProto.homeSettingsProto.copy {
                    this.gridItemSettingsProto = this.gridItemSettingsProto.copy {
                        this.iconSize = iconSize
                    }
                }
            }
        }
    }

    suspend fun updateTextSize(textSize: Int) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                homeSettingsProto = userDataProto.homeSettingsProto.copy {
                    this.gridItemSettingsProto = this.gridItemSettingsProto.copy {
                        this.textSize = textSize
                    }
                }
            }
        }
    }

    suspend fun updateShowLabel(showLabel: Boolean) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                homeSettingsProto = userDataProto.homeSettingsProto.copy {
                    this.gridItemSettingsProto = this.gridItemSettingsProto.copy {
                        this.showLabel = showLabel
                    }
                }
            }
        }
    }

    suspend fun updateSingleLineLabel(singleLineLabel: Boolean) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                homeSettingsProto = userDataProto.homeSettingsProto.copy {
                    this.gridItemSettingsProto = this.gridItemSettingsProto.copy {
                        this.singleLineLabel = singleLineLabel
                    }
                }
            }
        }
    }

    suspend fun updateThemeBrand(themeBrand: ThemeBrand) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                generalSettingsProto = userDataProto.generalSettingsProto.copy {
                    this.themeBrandProto = themeBrand.toThemeBrandProto()
                }
            }
        }
    }

    suspend fun updateDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                generalSettingsProto = userDataProto.generalSettingsProto.copy {
                    this.darkThemeConfigProto = darkThemeConfig.toDarkThemeConfigProto()
                }
            }
        }
    }

    suspend fun updateDynamicTheme(dynamicTheme: Boolean) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                generalSettingsProto = userDataProto.generalSettingsProto.copy {
                    this.dynamicTheme = dynamicTheme
                }
            }
        }
    }

    suspend fun updateAppDrawerTextColor(textColor: TextColor) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                appDrawerSettingsProto = userDataProto.appDrawerSettingsProto.copy {
                    this.gridItemSettingsProto = this.gridItemSettingsProto.copy {
                        this.textColorProto = textColor.toTextColorProto()
                    }
                }
            }
        }
    }

    suspend fun updateAppDrawerIconSize(iconSize: Int) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                appDrawerSettingsProto = userDataProto.appDrawerSettingsProto.copy {
                    this.gridItemSettingsProto = this.gridItemSettingsProto.copy {
                        this.iconSize = iconSize
                    }
                }
            }
        }
    }

    suspend fun updateAppDrawerTextSize(textSize: Int) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                appDrawerSettingsProto = userDataProto.appDrawerSettingsProto.copy {
                    this.gridItemSettingsProto = this.gridItemSettingsProto.copy {
                        this.textSize = textSize
                    }
                }
            }
        }
    }

    suspend fun updateAppDrawerShowLabel(showLabel: Boolean) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                appDrawerSettingsProto = userDataProto.appDrawerSettingsProto.copy {
                    this.gridItemSettingsProto = this.gridItemSettingsProto.copy {
                        this.showLabel = showLabel
                    }
                }
            }
        }
    }

    suspend fun updateAppDrawerSingleLineLabel(singleLineLabel: Boolean) {
        dataStore.updateData { userDataProto ->
            userDataProto.copy {
                appDrawerSettingsProto = userDataProto.appDrawerSettingsProto.copy {
                    this.gridItemSettingsProto = this.gridItemSettingsProto.copy {
                        this.singleLineLabel = singleLineLabel
                    }
                }
            }
        }
    }
}
