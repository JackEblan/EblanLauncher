package com.eblan.launcher.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.eblan.launcher.domain.framework.PerformGlobalAction
import com.eblan.launcher.domain.model.GlobalAction
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
internal class EblanAccessibilityService : AccessibilityService() {
    @Inject
    lateinit var performGlobalAction: PerformGlobalAction

    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

    }

    override fun onInterrupt() {

    }

    override fun onServiceConnected() {
        serviceScope.launch {
            performGlobalAction.globalAction.collect { globalAction ->
                when (globalAction) {
                    GlobalAction.Notifications -> {
                        performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
                    }

                    GlobalAction.QuickSettings -> {

                    }

                    GlobalAction.LockScreen -> {

                    }
                }
            }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
    }
}