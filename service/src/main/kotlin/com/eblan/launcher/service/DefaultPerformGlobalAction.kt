package com.eblan.launcher.service

import com.eblan.launcher.domain.framework.PerformGlobalAction
import com.eblan.launcher.domain.model.GlobalAction
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

internal class DefaultPerformGlobalAction @Inject constructor(): PerformGlobalAction {
    private val _globalAction = MutableSharedFlow<GlobalAction>()

    override val globalAction = _globalAction.asSharedFlow()

    override suspend fun performGlobalAction(globalAction: GlobalAction){
        _globalAction.emit(globalAction)
    }
}