package com.eblan.launcher.data.datastore.mapper

import com.eblan.launcher.data.datastore.proto.appdrawer.AppDrawerSettingsProto
import com.eblan.launcher.data.datastore.proto.gesture.GestureActionProto
import com.eblan.launcher.data.datastore.proto.gesture.GestureSettingsProto
import com.eblan.launcher.data.datastore.proto.gesture.NoneProto
import com.eblan.launcher.data.datastore.proto.gesture.OpenAppDrawerProto
import com.eblan.launcher.data.datastore.proto.gesture.OpenAppProto
import com.eblan.launcher.data.datastore.proto.gesture.OpenNotificationPanelProto
import com.eblan.launcher.data.datastore.proto.home.HomeSettingsProto
import com.eblan.launcher.data.datastore.proto.home.TextColorProto
import com.eblan.launcher.domain.model.AppDrawerSettings
import com.eblan.launcher.domain.model.GestureAction
import com.eblan.launcher.domain.model.GestureSettings
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.TextColor

internal fun HomeSettingsProto.toHomeSettings(): HomeSettings {
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

internal fun AppDrawerSettingsProto.toAppDrawerSettings(): AppDrawerSettings {
    return AppDrawerSettings(
        appDrawerColumns = appDrawerColumns,
        appDrawerRowsHeight = appDrawerRowsHeight,
    )
}

internal fun TextColorProto.toTextColor(): TextColor {
    return when (this) {
        TextColorProto.System, TextColorProto.UNRECOGNIZED -> TextColor.System
        TextColorProto.Light -> TextColor.Light
        TextColorProto.Dark -> TextColor.Dark
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