package com.eblan.launcher.domain.model

sealed interface PinItemRequestType {
    data class Widget(val className: String) : PinItemRequestType

    data class ShortcutInfo(
        val shortcutId: String,
        val packageName: String,
        val shortLabel: String,
        val longLabel: String,
    ): PinItemRequestType
}