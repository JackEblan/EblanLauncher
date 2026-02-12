/*
 *
 *   Copyright 2023 Einstein Blanco
 *
 *   Licensed under the GNU General Public License v3.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.gnu.org/licenses/gpl-3.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package com.eblan.launcher.feature.settings.settings

import android.content.Intent
import android.provider.Settings.ACTION_HOME_SETTINGS
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.ui.dialog.TextDialog
import com.eblan.launcher.ui.local.LocalPackageManager

@Composable
internal fun SettingsRoute(
    modifier: Modifier = Modifier,
    onFinish: () -> Unit,
    onGeneral: () -> Unit,
    onHome: () -> Unit,
    onAppDrawer: () -> Unit,
    onGestures: () -> Unit,
    onFolder: () -> Unit,
    onExperimental: () -> Unit,
) {
    SettingsScreen(
        modifier = modifier,
        onFinish = onFinish,
        onGeneral = onGeneral,
        onHome = onHome,
        onAppDrawer = onAppDrawer,
        onGestures = onGestures,
        onFolder = onFolder,
        onExperimental = onExperimental,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(
    modifier: Modifier = Modifier,
    onFinish: () -> Unit,
    onGeneral: () -> Unit,
    onHome: () -> Unit,
    onAppDrawer: () -> Unit,
    onGestures: () -> Unit,
    onFolder: () -> Unit,
    onExperimental: () -> Unit,
) {
    val context = LocalContext.current

    val packageManager = LocalPackageManager.current

    var showSupportDialog by remember { mutableStateOf(false) }

    BackHandler {
        onFinish()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Settings")
                },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
                        Icon(
                            imageVector = EblanLauncherIcons.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
            ) {
                SettingsRow(
                    imageVector = EblanLauncherIcons.Handshake,
                    title = "Support Development",
                    subtitle = "Developer information",
                    onClick = {
                        showSupportDialog = true
                    },
                )

                HorizontalDivider(modifier = Modifier.fillMaxWidth())

                if (!packageManager.isDefaultLauncher()) {
                    SettingsRow(
                        imageVector = EblanLauncherIcons.Info,
                        title = "Default Launcher",
                        subtitle = "Choose Yagni Launcher",
                        onClick = {
                            context.startActivity(Intent(ACTION_HOME_SETTINGS))
                        },
                    )

                    HorizontalDivider(modifier = Modifier.fillMaxWidth())
                }

                SettingsRow(
                    imageVector = EblanLauncherIcons.Settings,
                    title = "General",
                    subtitle = "Themes, icon packs",
                    onClick = onGeneral,
                )

                HorizontalDivider(modifier = Modifier.fillMaxWidth())

                SettingsRow(
                    imageVector = EblanLauncherIcons.Home,
                    title = "Home",
                    subtitle = "Grid, icon, dock, and more",
                    onClick = onHome,
                )

                HorizontalDivider(modifier = Modifier.fillMaxWidth())

                SettingsRow(
                    imageVector = EblanLauncherIcons.Apps,
                    title = "App Drawer",
                    subtitle = "Columns and rows count",
                    onClick = onAppDrawer,
                )

                HorizontalDivider(modifier = Modifier.fillMaxWidth())

                SettingsRow(
                    imageVector = EblanLauncherIcons.Folder,
                    title = "Folder",
                    subtitle = "Columns and rows count",
                    onClick = onFolder,
                )

                HorizontalDivider(modifier = Modifier.fillMaxWidth())

                SettingsRow(
                    imageVector = EblanLauncherIcons.Gesture,
                    title = "Gestures",
                    subtitle = "Swipe gesture actions",
                    onClick = onGestures,
                )

                HorizontalDivider(modifier = Modifier.fillMaxWidth())

                SettingsRow(
                    imageVector = EblanLauncherIcons.DeveloperMode,
                    title = "Experimental",
                    subtitle = "Advanced options for power users",
                    onClick = onExperimental,
                )
            }
        }
    }

    if (showSupportDialog) {
        TextDialog(
            title = "Support Development",
            text = "Thank you for using Yagni Launcher Alpha! I’ve been building this project since January 2025, releasing weekly updates. It’s my most complex project yet, and I pour my heart into it. If you enjoy it, you can support development with a donation or a star on GitHub.",
            onClick = {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        "https://github.com/JackEblan/YagniLauncher".toUri(),
                    ),
                )

                showSupportDialog = false
            },
            onDismissRequest = {
                showSupportDialog = false
            },
        )
    }
}

@Composable
private fun SettingsRow(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
        )

        Spacer(modifier = Modifier.width(20.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
