package com.eblan.launcher.data.datastore

import androidx.datastore.core.DataStore
import com.eblan.launcher.data.datastore.mapper.toAppDrawerSettings
import com.eblan.launcher.data.datastore.mapper.toGestureActionProto
import com.eblan.launcher.data.datastore.mapper.toGestureSettings
import com.eblan.launcher.data.datastore.mapper.toHomeSettings
import com.eblan.launcher.data.datastore.proto.UserDataProto
import com.eblan.launcher.data.datastore.proto.copy
import com.eblan.launcher.data.datastore.proto.gesture.copy
import com.eblan.launcher.data.datastore.proto.home.copy
import com.eblan.launcher.domain.model.GestureAction
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
}