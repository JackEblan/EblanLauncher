package com.eblan.launcher.domain.model

data class GestureSettings(
    val doubleTap: GestureAction,
    val swipeUp: GestureAction,
    val swipeDown: GestureAction,
)

sealed interface GestureAction {
    data object None : GestureAction

    data object OpenAppDrawer : GestureAction

    data object OpenNotificationPanel : GestureAction

    data class OpenApp(val componentName: String) : GestureAction
}