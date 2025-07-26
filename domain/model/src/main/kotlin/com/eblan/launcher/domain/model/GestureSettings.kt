package com.eblan.launcher.domain.model

data class GestureSettings(
    val swipeUp: GestureAction,
    val swipeDown: GestureAction,
)

sealed interface GestureAction {
    data object None : GestureAction

    data object OpenAppDrawer : GestureAction

    data object OpenNotificationPanel : GestureAction

    data class OpenApp(val packageName: String) : GestureAction
}