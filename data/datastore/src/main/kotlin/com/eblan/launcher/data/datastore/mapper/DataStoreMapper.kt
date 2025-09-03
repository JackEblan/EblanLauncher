package com.eblan.launcher.data.datastore.mapper

import com.eblan.launcher.data.datastore.proto.appdrawer.AppDrawerSettingsProto
import com.eblan.launcher.data.datastore.proto.general.DarkThemeConfigProto
import com.eblan.launcher.data.datastore.proto.general.GeneralSettingsProto
import com.eblan.launcher.data.datastore.proto.general.ThemeBrandProto
import com.eblan.launcher.data.datastore.proto.gesture.GestureActionProto
import com.eblan.launcher.data.datastore.proto.gesture.GestureSettingsProto
import com.eblan.launcher.data.datastore.proto.gesture.NoneProto
import com.eblan.launcher.data.datastore.proto.gesture.OpenAppDrawerProto
import com.eblan.launcher.data.datastore.proto.gesture.OpenAppProto
import com.eblan.launcher.data.datastore.proto.gesture.OpenNotificationPanelProto
import com.eblan.launcher.data.datastore.proto.home.GridItemSettingsProto
import com.eblan.launcher.data.datastore.proto.home.HomeSettingsProto
import com.eblan.launcher.data.datastore.proto.home.TextColorProto
import com.eblan.launcher.domain.model.AppDrawerSettings
import com.eblan.launcher.domain.model.DarkThemeConfig
import com.eblan.launcher.domain.model.GeneralSettings
import com.eblan.launcher.domain.model.GestureAction
import com.eblan.launcher.domain.model.GestureSettings
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.domain.model.ThemeBrand

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
    )
}

internal fun GridItemSettingsProto.toGridItemSettings(): GridItemSettings {
    return GridItemSettings(
        iconSize = iconSize,
        textColor = textColorProto.toTextColor(),
        textSize = textSize,
        showLabel = showLabel,
        singleLineLabel = singleLineLabel,
    )
}

internal fun GeneralSettingsProto.toGeneralSettings(): GeneralSettings {
    return GeneralSettings(
        themeBrand = themeBrandProto.toThemeBrand(),
        darkThemeConfig = darkThemeConfigProto.toDarkThemeConfig(),
        dynamicTheme = dynamicTheme,
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

internal fun GestureAction.toGestureActionProto(): GestureActionProto {
    return when (this) {
        GestureAction.None -> GestureActionProto.newBuilder()
            .setNoneProto(NoneProto.getDefaultInstance())
            .build()

        is GestureAction.OpenAppDrawer -> GestureActionProto.newBuilder()
            .setOpenAppDrawerProto(OpenAppDrawerProto.getDefaultInstance())
            .build()

        is GestureAction.OpenNotificationPanel -> GestureActionProto.newBuilder()
            .setOpenNotificationPanelProto(OpenNotificationPanelProto.getDefaultInstance())
            .build()

        is GestureAction.OpenApp -> GestureActionProto.newBuilder()
            .setOpenAppProto(OpenAppProto.newBuilder().setComponentName(componentName))
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
        GestureActionProto.TypeCase.NONEPROTO -> GestureAction.None

        GestureActionProto.TypeCase.OPENAPPDRAWERPROTO ->
            GestureAction.OpenAppDrawer

        GestureActionProto.TypeCase.OPENNOTIFICATIONPANELPROTO ->
            GestureAction.OpenNotificationPanel

        GestureActionProto.TypeCase.OPENAPPPROTO ->
            GestureAction.OpenApp(openAppProto.componentName)

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