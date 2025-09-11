package com.eblan.launcher.domain.framework

import com.eblan.launcher.domain.model.GlobalAction
import kotlinx.coroutines.flow.Flow

interface PerformGlobalAction {
    val globalAction: Flow<GlobalAction>

    suspend fun performGlobalAction(globalAction: GlobalAction)
}