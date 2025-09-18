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
package com.eblan.launcher.data.datastore.mapper

import com.eblan.launcher.data.datastore.proto.appdrawer.AppDrawerSettingsProto
import com.eblan.launcher.data.datastore.proto.general.DarkThemeConfigProto
import com.eblan.launcher.data.datastore.proto.general.GeneralSettingsProto
import com.eblan.launcher.data.datastore.proto.general.ThemeBrandProto
import com.eblan.launcher.data.datastore.proto.gesture.GestureActionProto
import com.eblan.launcher.data.datastore.proto.gesture.GestureSettingsProto
import com.eblan.launcher.data.datastore.proto.gesture.LockScreenProto
import com.eblan.launcher.data.datastore.proto.gesture.NoneProto
import com.eblan.launcher.data.datastore.proto.gesture.OpenAppDrawerProto
import com.eblan.launcher.data.datastore.proto.gesture.OpenAppProto
import com.eblan.launcher.data.datastore.proto.gesture.OpenNotificationPanelProto
import com.eblan.launcher.data.datastore.proto.gesture.OpenQuickSettingsProto
import com.eblan.launcher.data.datastore.proto.gesture.OpenRecentsProto
import com.eblan.launcher.data.datastore.proto.home.GridItemSettingsProto
import com.eblan.launcher.data.datastore.proto.home.HomeSettingsProto
import com.eblan.launcher.data.datastore.proto.home.HorizontalAlignmentProto
import com.eblan.launcher.data.datastore.proto.home.TextColorProto
import com.eblan.launcher.data.datastore.proto.home.VerticalArrangementProto
import com.eblan.launcher.domain.model.AppDrawerSettings
import com.eblan.launcher.domain.model.DarkThemeConfig
import com.eblan.launcher.domain.model.GeneralSettings
import com.eblan.launcher.domain.model.GestureAction
import com.eblan.launcher.domain.model.GestureAction.LockScreen
import com.eblan.launcher.domain.model.GestureAction.None
import com.eblan.launcher.domain.model.GestureAction.OpenApp
import com.eblan.launcher.domain.model.GestureAction.OpenAppDrawer
import com.eblan.launcher.domain.model.GestureAction.OpenNotificationPanel
import com.eblan.launcher.domain.model.GestureAction.OpenQuickSettings
import com.eblan.launcher.domain.model.GestureAction.OpenRecents
import com.eblan.launcher.domain.model.GestureSettings
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.HorizontalAlignment
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.domain.model.ThemeBrand
import com.eblan.launcher.domain.model.VerticalArrangement

internal fun HomeSettingsProto.toHomeSettings(): HomeSettings {
    return HomeSettings(
        rows = rows,
        columns = columns,
        pageCount = pageCount,
        infiniteScroll = infiniteScroll,
        dockRows = dockRows,
        dockColumns = dockColumns,
        dockHeight = dockHeight,
        initialPage = initialPage,
        wallpaperScroll = wallpaperScroll,
        folderRows = folderRows,
        folderColumns = folderColumns,
        gridItemSettings = gridItemSettingsProto.toGridItemSettings(),
    )
}

internal fun AppDrawerSettingsProto.toAppDrawerSettings(): AppDrawerSettings {
    return AppDrawerSettings(
        appDrawerColumns = appDrawerColumns,
        appDrawerRowsHeight = appDrawerRowsHeight,
        gridItemSettings = gridItemSettingsProto.toGridItemSettings(),
    )
}

internal fun GridItemSettingsProto.toGridItemSettings(): GridItemSettings {
    return GridItemSettings(
        iconSize = iconSize,
        textColor = textColorProto.toTextColor(),
        textSize = textSize,
        showLabel = showLabel,
        singleLineLabel = singleLineLabel,
        horizontalAlignment = horizontalAlignmentProto.toHorizontalAlignment(),
        verticalArrangement = verticalArrangementProto.toVerticalArrangement(),
    )
}

internal fun GeneralSettingsProto.toGeneralSettings(): GeneralSettings {
    return GeneralSettings(
        themeBrand = themeBrandProto.toThemeBrand(),
        darkThemeConfig = darkThemeConfigProto.toDarkThemeConfig(),
        dynamicTheme = dynamicTheme,
        iconPackInfoPackageName = iconPackInfoPackageName,
    )
}

internal fun TextColor.toTextColorProto(): TextColorProto {
    return when (this) {
        TextColor.System -> TextColorProto.TextColorSystem
        TextColor.Light -> TextColorProto.TextColorLight
        TextColor.Dark -> TextColorProto.TextColorDark
    }
}

internal fun TextColorProto.toTextColor(): TextColor {
    return when (this) {
        TextColorProto.TextColorSystem, TextColorProto.UNRECOGNIZED -> TextColor.System
        TextColorProto.TextColorLight -> TextColor.Light
        TextColorProto.TextColorDark -> TextColor.Dark
    }
}

internal fun HorizontalAlignment.toHorizontalAlignmentProto(): HorizontalAlignmentProto {
    return when (this) {
        HorizontalAlignment.Start -> HorizontalAlignmentProto.Start
        HorizontalAlignment.CenterHorizontally -> HorizontalAlignmentProto.CenterHorizontally
        HorizontalAlignment.End -> HorizontalAlignmentProto.End
    }
}

internal fun HorizontalAlignmentProto.toHorizontalAlignment(): HorizontalAlignment {
    return when (this) {
        HorizontalAlignmentProto.Start -> HorizontalAlignment.Start
        HorizontalAlignmentProto.CenterHorizontally, HorizontalAlignmentProto.UNRECOGNIZED -> HorizontalAlignment.CenterHorizontally
        HorizontalAlignmentProto.End -> HorizontalAlignment.End
    }
}

internal fun VerticalArrangement.toVerticalArrangementProto(): VerticalArrangementProto {
    return when (this) {
        VerticalArrangement.Top -> VerticalArrangementProto.Top
        VerticalArrangement.Center -> VerticalArrangementProto.Center
        VerticalArrangement.Bottom -> VerticalArrangementProto.Bottom
    }
}

internal fun VerticalArrangementProto.toVerticalArrangement(): VerticalArrangement {
    return when (this) {
        VerticalArrangementProto.Top -> VerticalArrangement.Top
        VerticalArrangementProto.Center, VerticalArrangementProto.UNRECOGNIZED -> VerticalArrangement.Center
        VerticalArrangementProto.Bottom -> VerticalArrangement.Bottom
    }
}

internal fun GestureAction.toGestureActionProto(): GestureActionProto {
    return when (this) {
        None -> GestureActionProto.newBuilder()
            .setNoneProto(NoneProto.getDefaultInstance())
            .build()

        is OpenAppDrawer -> GestureActionProto.newBuilder()
            .setOpenAppDrawerProto(OpenAppDrawerProto.getDefaultInstance())
            .build()

        is OpenNotificationPanel -> GestureActionProto.newBuilder()
            .setOpenNotificationPanelProto(OpenNotificationPanelProto.getDefaultInstance())
            .build()

        is OpenApp -> GestureActionProto.newBuilder()
            .setOpenAppProto(OpenAppProto.newBuilder().setComponentName(componentName))
            .build()

        LockScreen -> GestureActionProto.newBuilder()
            .setLockScreenProto(LockScreenProto.getDefaultInstance())
            .build()

        OpenQuickSettings -> GestureActionProto.newBuilder()
            .setOpenQuickSettingsProto(OpenQuickSettingsProto.getDefaultInstance())
            .build()

        OpenRecents -> GestureActionProto.newBuilder()
            .setOpenRecentsProto(OpenRecentsProto.getDefaultInstance())
            .build()
    }
}

internal fun GestureSettingsProto.toGestureSettings(): GestureSettings {
    return GestureSettings(
        doubleTap = doubleTapProto.toGestureAction(),
        swipeUp = swipeUpProto.toGestureAction(),
        swipeDown = swipeDownProto.toGestureAction(),
    )
}

internal fun GestureActionProto.toGestureAction(): GestureAction {
    return when (typeCase) {
        GestureActionProto.TypeCase.NONEPROTO -> None

        GestureActionProto.TypeCase.OPENAPPDRAWERPROTO -> OpenAppDrawer

        GestureActionProto.TypeCase.OPENNOTIFICATIONPANELPROTO -> OpenNotificationPanel

        GestureActionProto.TypeCase.OPENAPPPROTO ->
            OpenApp(openAppProto.componentName)

        GestureActionProto.TypeCase.LOCKSCREENPROTO -> LockScreen
        GestureActionProto.TypeCase.OPENQUICKSETTINGSPROTO -> OpenQuickSettings
        GestureActionProto.TypeCase.OPENRECENTSPROTO -> OpenRecents

        GestureActionProto.TypeCase.TYPE_NOT_SET, null ->
            error("GestureActionProto type not set")
    }
}

internal fun ThemeBrand.toThemeBrandProto(): ThemeBrandProto {
    return when (this) {
        ThemeBrand.Green -> ThemeBrandProto.Green
        ThemeBrand.Purple -> ThemeBrandProto.Purple
    }
}

internal fun ThemeBrandProto.toThemeBrand(): ThemeBrand {
    return when (this) {
        ThemeBrandProto.Green, ThemeBrandProto.UNRECOGNIZED -> ThemeBrand.Green
        ThemeBrandProto.Purple -> ThemeBrand.Purple
    }
}

internal fun DarkThemeConfig.toDarkThemeConfigProto(): DarkThemeConfigProto {
    return when (this) {
        DarkThemeConfig.System -> DarkThemeConfigProto.DarkThemeConfigSystem
        DarkThemeConfig.Light -> DarkThemeConfigProto.DarkThemeConfigLight
        DarkThemeConfig.Dark -> DarkThemeConfigProto.DarkThemeConfigDark
    }
}

internal fun DarkThemeConfigProto.toDarkThemeConfig(): DarkThemeConfig {
    return when (this) {
        DarkThemeConfigProto.DarkThemeConfigSystem, DarkThemeConfigProto.UNRECOGNIZED -> DarkThemeConfig.System
        DarkThemeConfigProto.DarkThemeConfigLight -> DarkThemeConfig.Light
        DarkThemeConfigProto.DarkThemeConfigDark -> DarkThemeConfig.Dark
    }
}

internal fun GridItemSettings.toGridItemSettingsProto(): GridItemSettingsProto {
    return GridItemSettingsProto.newBuilder()
        .setIconSize(iconSize)
        .setTextColorProto(textColor.toTextColorProto())
        .setTextSize(textSize)
        .setShowLabel(showLabel)
        .setSingleLineLabel(singleLineLabel)
        .setHorizontalAlignmentProto(horizontalAlignment.toHorizontalAlignmentProto())
        .setVerticalArrangementProto(verticalArrangement.toVerticalArrangementProto())
        .build()
}

internal fun HomeSettings.toHomeSettingsProto(): HomeSettingsProto {
    return HomeSettingsProto.newBuilder()
        .setRows(rows)
        .setColumns(columns)
        .setPageCount(pageCount)
        .setInfiniteScroll(infiniteScroll)
        .setDockRows(dockRows)
        .setDockColumns(dockColumns)
        .setDockHeight(dockHeight)
        .setInitialPage(initialPage)
        .setWallpaperScroll(wallpaperScroll)
        .setFolderRows(folderRows)
        .setFolderColumns(folderColumns)
        .setGridItemSettingsProto(gridItemSettings.toGridItemSettingsProto())
        .build()
}

internal fun AppDrawerSettings.toAppDrawerSettingsProto(): AppDrawerSettingsProto {
    val gridItemSettingsProto = GridItemSettingsProto.newBuilder()
        .setIconSize(gridItemSettings.iconSize)
        .setTextColorProto(gridItemSettings.textColor.toTextColorProto())
        .setTextSize(gridItemSettings.textSize)
        .setShowLabel(gridItemSettings.showLabel)
        .setSingleLineLabel(gridItemSettings.singleLineLabel)
        .setHorizontalAlignmentProto(gridItemSettings.horizontalAlignment.toHorizontalAlignmentProto())
        .setVerticalArrangementProto(gridItemSettings.verticalArrangement.toVerticalArrangementProto())
        .build()

    return AppDrawerSettingsProto.newBuilder()
        .setAppDrawerColumns(appDrawerColumns)
        .setAppDrawerRowsHeight(appDrawerRowsHeight)
        .setGridItemSettingsProto(gridItemSettingsProto)
        .build()
}

internal fun GeneralSettings.toGeneralSettingsProto(): GeneralSettingsProto {
    return GeneralSettingsProto.newBuilder()
        .setThemeBrandProto(themeBrand.toThemeBrandProto())
        .setDarkThemeConfigProto(darkThemeConfig.toDarkThemeConfigProto())
        .setDynamicTheme(dynamicTheme)
        .setIconPackInfoPackageName(iconPackInfoPackageName)
        .build()
}

internal fun GestureSettings.toGestureSettingsProto(): GestureSettingsProto {
    return GestureSettingsProto.newBuilder()
        .setDoubleTapProto(doubleTap.toGestureActionProto())
        .setSwipeUpProto(swipeUp.toGestureActionProto())
        .setSwipeDownProto(swipeDown.toGestureActionProto())
        .build()
}
