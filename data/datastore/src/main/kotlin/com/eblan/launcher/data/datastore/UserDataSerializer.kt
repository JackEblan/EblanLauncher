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

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.eblan.launcher.data.datastore.mapper.toDarkThemeConfigProto
import com.eblan.launcher.data.datastore.mapper.toGestureActionProto
import com.eblan.launcher.data.datastore.mapper.toThemeBrandProto
import com.eblan.launcher.data.datastore.proto.UserDataProto
import com.eblan.launcher.data.datastore.proto.appdrawer.AppDrawerSettingsProto
import com.eblan.launcher.data.datastore.proto.general.GeneralSettingsProto
import com.eblan.launcher.data.datastore.proto.gesture.GestureSettingsProto
import com.eblan.launcher.data.datastore.proto.home.GridItemSettingsProto
import com.eblan.launcher.data.datastore.proto.home.HomeSettingsProto
import com.eblan.launcher.data.datastore.proto.home.TextColorProto
import com.eblan.launcher.domain.model.DarkThemeConfig
import com.eblan.launcher.domain.model.GestureAction
import com.eblan.launcher.domain.model.ThemeBrand
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class UserDataSerializer @Inject constructor() : Serializer<UserDataProto> {
    private val defaultGeneralSettingsProto = GeneralSettingsProto.newBuilder().apply {
        themeBrandProto = ThemeBrand.Green.toThemeBrandProto()

        darkThemeConfigProto = DarkThemeConfig.System.toDarkThemeConfigProto()

        dynamicTheme = false

        iconPackPackageName = ""
    }.build()

    private val defaultGridItemSettingsProto = GridItemSettingsProto.newBuilder().apply {
        iconSize = 150
        textColorProto = TextColorProto.TextColorSystem
        textSize = 30
        showLabel = true
        singleLineLabel = true
    }.build()

    private val defaultHomeSettingsProto = HomeSettingsProto.newBuilder().apply {
        rows = 5
        columns = 5
        pageCount = 1
        infiniteScroll = false
        dockRows = 1
        dockColumns = 5
        dockHeight = 300
        initialPage = 0
        wallpaperScroll = false
        folderRows = 5
        folderColumns = 5
        gridItemSettingsProto = defaultGridItemSettingsProto
    }.build()

    private val defaultAppDrawerSettingsProto = AppDrawerSettingsProto.newBuilder().apply {
        appDrawerColumns = 5
        appDrawerRowsHeight = 400
        gridItemSettingsProto = defaultGridItemSettingsProto
    }.build()

    private val defaultGestureSettingsProto = GestureSettingsProto.newBuilder().apply {
        doubleTapProto = GestureAction.None.toGestureActionProto()

        swipeUpProto = GestureAction.OpenAppDrawer.toGestureActionProto()

        swipeDownProto = GestureAction.None.toGestureActionProto()
    }.build()

    override val defaultValue: UserDataProto = UserDataProto.newBuilder().apply {
        homeSettingsProto = defaultHomeSettingsProto

        appDrawerSettingsProto = defaultAppDrawerSettingsProto

        gestureSettingsProto = defaultGestureSettingsProto

        generalSettingsProto = defaultGeneralSettingsProto
    }.build()

    override suspend fun readFrom(input: InputStream): UserDataProto = try {
        UserDataProto.parseFrom(input)
    } catch (exception: InvalidProtocolBufferException) {
        throw CorruptionException("Cannot read proto.", exception)
    }

    override suspend fun writeTo(t: UserDataProto, output: OutputStream) {
        t.writeTo(output)
    }
}
