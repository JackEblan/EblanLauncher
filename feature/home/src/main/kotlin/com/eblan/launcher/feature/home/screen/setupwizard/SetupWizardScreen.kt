package com.eblan.launcher.feature.home.screen.setupwizard

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.provider.Settings.ACTION_HOME_SETTINGS
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.feature.home.R
import com.eblan.launcher.ui.local.LocalPackageManager
import com.eblan.launcher.ui.settings.SettingsColumn
import com.eblan.launcher.ui.settings.SettingsSwitch
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

@Composable
internal fun SetupWizardScreen(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
) {
    val scope = rememberCoroutineScope()

    val horizontalPagerState = rememberPagerState(
        pageCount = {
            2
        },
    )

    val nextLabel by remember {
        derivedStateOf {
            if (horizontalPagerState.currentPage < horizontalPagerState.pageCount - 1) {
                "Next"
            } else {
                "Finish"
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalPager(
                state = horizontalPagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false,
            ) { index ->
                when (index) {
                    0 -> {
                        WelcomePage()
                    }

                    1 -> {
                        PermissionPage()
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                TextButton(onClick = {
                    scope.launch {
                        horizontalPagerState.animateScrollToPage(horizontalPagerState.currentPage - 1)
                    }
                }) {
                    Text(text = "Previous")
                }

                Button(onClick = {
                    scope.launch {
                        if (horizontalPagerState.currentPage < horizontalPagerState.pageCount - 1) {
                            horizontalPagerState.animateScrollToPage(horizontalPagerState.currentPage + 1)
                        } else {
                            println("Finish")
                        }
                    }
                }) {
                    Text(text = nextLabel)
                }
            }
        }
    }
}

@Composable
private fun WelcomePage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_welcome),
            contentDescription = null,
        )

        Text(
            text = "Welcome to",
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Eblan Launcher",
            style = MaterialTheme.typography.headlineLarge,
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionPage(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val packageManager = LocalPackageManager.current

    val notificationsPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    } else {
        null
    }

    val phonePermissionState =
        rememberPermissionState(permission = Manifest.permission.CALL_PHONE)


    Column(modifier = modifier.fillMaxSize()) {
        if (notificationsPermissionState != null) {
            SettingsSwitch(
                modifier = Modifier.padding(
                    horizontal = 10.dp,
                    vertical = 5.dp,
                ),
                checked = notificationsPermissionState.status.isGranted,
                title = "Notifications",
                subtitle = "Post notifications",
                onCheckedChange = {
                    if (!notificationsPermissionState.status.isGranted) {
                        notificationsPermissionState.launchPermissionRequest()
                    }
                }
            )
        }

        SettingsSwitch(
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 5.dp,
            ),
            checked = phonePermissionState.status.isGranted,
            title = "Call Phone",
            subtitle = "Phone shortcuts",
            onCheckedChange = {
                if (!phonePermissionState.status.isGranted) {
                    phonePermissionState.launchPermissionRequest()
                }
            }
        )

        SettingsColumn(
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 5.dp,
            ),
            title = "Accessibility Services",
            subtitle = "Permission to perform global actions",
            onClick = {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                context.startActivity(intent)
            },
        )

        if (!packageManager.isDefaultLauncher()) {
            SettingsColumn(
                modifier = Modifier.padding(
                    horizontal = 10.dp,
                    vertical = 5.dp,
                ),
                title = "Default Launcher",
                subtitle = "Choose Eblan Launcher",
                onClick = {
                    context.startActivity(Intent(ACTION_HOME_SETTINGS))
                },
            )
        }
    }
}