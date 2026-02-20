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
package com.eblan.launcher.feature.home.screen.application.draganddrop

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.addLastModifiedToFileCacheKey
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.AppDrawerSettings
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.EblanUser
import com.eblan.launcher.feature.home.util.getHorizontalAlignment
import com.eblan.launcher.feature.home.util.getSystemTextColor
import com.eblan.launcher.feature.home.util.getVerticalArrangement
import com.eblan.launcher.ui.local.LocalLauncherApps
import kotlin.uuid.ExperimentalUuidApi

internal fun LazyGridScope.dragAndDropPrivateSpace(
    privateEblanUser: EblanUser?,
    privateEblanApplicationInfos: List<EblanApplicationInfo>,
    isQuietModeEnabled: Boolean,
    appDrawerSettings: AppDrawerSettings,
    iconPackFilePaths: Map<String, String>,
) {
    if (privateEblanUser == null) return

    if (privateEblanUser.isPrivateSpaceEntryPointHidden) return

    stickyHeader {
        PrivateSpaceStickyHeader(privateEblanUser = privateEblanUser)
    }

    if (!isQuietModeEnabled) {
        items(privateEblanApplicationInfos) { eblanApplicationInfo ->
            PrivateSpaceEblanApplicationInfoItem(
                eblanApplicationInfo = eblanApplicationInfo,
                appDrawerSettings = appDrawerSettings,
                iconPackFilePaths = iconPackFilePaths,
            )
        }
    }
}

@Composable
private fun PrivateSpaceStickyHeader(
    modifier: Modifier = Modifier,
    privateEblanUser: EblanUser?,
) {
    if (privateEblanUser == null) return

    val launcherApps = LocalLauncherApps.current

    val privateSpaceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) {}

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Private",
        )

        Row {
            launcherApps.getPrivateSpaceSettingsIntent()?.let { intentSender ->
                IconButton(
                    onClick = {
                        privateSpaceLauncher.launch(
                            IntentSenderRequest.Builder(intentSender).build(),
                        )
                    },
                ) {
                    Icon(
                        imageVector = EblanLauncherIcons.Settings,
                        contentDescription = null,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun PrivateSpaceEblanApplicationInfoItem(
    modifier: Modifier = Modifier,
    eblanApplicationInfo: EblanApplicationInfo,
    appDrawerSettings: AppDrawerSettings,
    iconPackFilePaths: Map<String, String>,
) {
    val textColor = getSystemTextColor(
        systemTextColor = appDrawerSettings.gridItemSettings.textColor,
        systemCustomTextColor = appDrawerSettings.gridItemSettings.customTextColor,
    )

    val appDrawerRowsHeight = appDrawerSettings.appDrawerRowsHeight.dp

    val maxLines = if (appDrawerSettings.gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    val icon = iconPackFilePaths[eblanApplicationInfo.componentName] ?: eblanApplicationInfo.icon

    val horizontalAlignment =
        getHorizontalAlignment(horizontalAlignment = appDrawerSettings.gridItemSettings.horizontalAlignment)

    val verticalArrangement =
        getVerticalArrangement(verticalArrangement = appDrawerSettings.gridItemSettings.verticalArrangement)

    Column(
        modifier = modifier
            .height(appDrawerRowsHeight)
            .padding(appDrawerSettings.gridItemSettings.padding.dp)
            .background(
                color = Color(appDrawerSettings.gridItemSettings.customBackgroundColor),
                shape = RoundedCornerShape(size = appDrawerSettings.gridItemSettings.cornerRadius.dp),
            ),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        Box(
            modifier = Modifier.size(appDrawerSettings.gridItemSettings.iconSize.dp),
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(eblanApplicationInfo.customIcon ?: icon)
                    .addLastModifiedToFileCacheKey(true).build(),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
            )

            ElevatedCard(
                modifier = Modifier
                    .size((appDrawerSettings.gridItemSettings.iconSize * 0.40).dp)
                    .align(Alignment.BottomEnd),
            ) {
                Icon(
                    imageVector = EblanLauncherIcons.Work,
                    contentDescription = null,
                    modifier = Modifier.padding(2.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = eblanApplicationInfo.customLabel ?: eblanApplicationInfo.label,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = maxLines,
            fontSize = appDrawerSettings.gridItemSettings.textSize.sp,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
