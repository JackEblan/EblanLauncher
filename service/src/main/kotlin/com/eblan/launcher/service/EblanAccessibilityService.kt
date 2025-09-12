package com.eblan.launcher.service

import android.accessibilityservice.AccessibilityService
import android.os.Build
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
                        performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
                    }

                    GlobalAction.LockScreen -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                        }
                    }

                    GlobalAction.Recents -> {
                        performGlobalAction(GLOBAL_ACTION_RECENTS)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
    }
}