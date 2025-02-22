package com.eblan.launcher.feature.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Composable
fun rememberApplicationScreenUiState(): ApplicationScreenUiState {
    return rememberSaveable(saver = ApplicationScreenUiState.Saver) {
        ApplicationScreenUiState()
    }
}

@Stable
class ApplicationScreenUiState {
    var packageName by mutableStateOf<String?>(null)

    var flags by mutableStateOf<Int?>(null)

    var icon by mutableStateOf<ByteArray?>(null)

    var label by mutableStateOf("")

    fun validate(): Boolean {
        if (packageName.isNullOrBlank()) return false

        if (flags == null) return false

        if (icon == null) return false

        if (label.isBlank()) return false

        return true
    }

    companion object {
        val Saver = listSaver<ApplicationScreenUiState, Any?>(
            save = { state ->
                listOf(
                    state.icon,
                    state.label,
                )
            },
            restore = {
                ApplicationScreenUiState().apply {
                    icon = it[0] as ByteArray

                    label = it[1] as String
                }
            },
        )
    }
}