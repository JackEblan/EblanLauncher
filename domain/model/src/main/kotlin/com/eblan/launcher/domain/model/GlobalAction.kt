package com.eblan.launcher.domain.model

enum class GlobalAction {
    Notifications,
    QuickSettings,
    LockScreen;

    companion object {
        const val ACTION = "GlobalAction"
    }
}