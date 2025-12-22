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
package com.eblan.launcher.feature.home.screen.folder

import android.content.Intent
import android.graphics.Rect
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.util.Consumer
import com.eblan.launcher.domain.model.FolderDataById
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.grid.InteractiveGridItemContent
import com.eblan.launcher.feature.home.component.indicator.PageIndicator
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.util.getSystemTextColor
import com.eblan.launcher.feature.home.util.handleActionMainIntent
import com.eblan.launcher.ui.local.LocalLauncherApps
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.FolderScreen(
    modifier: Modifier = Modifier,
    foldersDataById: ArrayDeque<FolderDataById>,
    drag: Drag,
    paddingValues: PaddingValues,
    hasShortcutHostPermission: Boolean,
    screenWidth: Int,
    screenHeight: Int,
    textColor: TextColor,
    homeSettings: HomeSettings,
    folderGridHorizontalPagerState: PagerState,
    statusBarNotifications: Map<String, Int>,
    iconPackFilePaths: Map<String, String>,
    onUpdateScreen: (Screen) -> Unit,
    onRemoveLastFolder: () -> Unit,
    onAddFolder: (String) -> Unit,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onDraggingGridItem: (List<GridItem>) -> Unit,
    onResetOverlay: () -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
) {
    val density = LocalDensity.current

    val context = LocalContext.current

    val launcherApps = LocalLauncherApps.current

    val leftPadding = with(density) {
        paddingValues.calculateStartPadding(LayoutDirection.Ltr).roundToPx()
    }

    val rightPadding = with(density) {
        paddingValues.calculateEndPadding(LayoutDirection.Ltr).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val bottomPadding = with(density) {
        paddingValues.calculateBottomPadding().roundToPx()
    }

    val horizontalPadding = leftPadding + rightPadding

    val verticalPadding = topPadding + bottomPadding

    val gridWidth = screenWidth - horizontalPadding

    val gridHeight = screenHeight - verticalPadding

    var titleHeight by remember { mutableIntStateOf(0) }

    val folderDataById = foldersDataById.lastOrNull()

    val activity = LocalActivity.current as ComponentActivity

    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = foldersDataById) {
        if (foldersDataById.isEmpty()) {
            onUpdateScreen(Screen.Pager)
        }
    }

    DisposableEffect(key1 = activity) {
        val listener = Consumer<Intent> { intent ->
            scope.launch {
                handleActionMainIntent(
                    intent = intent,
                    onUpdateScreen = onUpdateScreen,
                )
            }
        }

        activity.addOnNewIntentListener(listener)

        onDispose {
            activity.removeOnNewIntentListener(listener)
        }
    }

    BackHandler(foldersDataById.isNotEmpty()) {
        onRemoveLastFolder()
    }

    val pageIndicatorHeight = 30.dp

    val pageIndicatorHeightPx = with(density) {
        pageIndicatorHeight.roundToPx()
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (folderDataById != null) {
            AnimatedContent(
                modifier = Modifier
                    .matchParentSize()
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding(),
                    ),
                targetState = folderDataById,
            ) { targetState ->
                Column(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .onSizeChanged {
                                titleHeight = it.height
                            }
                            .padding(5.dp),
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = targetState.label,
                            color = getSystemTextColor(textColor = textColor),
                            style = MaterialTheme.typography.headlineLarge,
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    HorizontalPager(
                        state = folderGridHorizontalPagerState,
                        modifier = Modifier.weight(1f),
                    ) { index ->
                        GridLayout(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                                    end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                                ),
                            gridItems = targetState.gridItemsByPage[index],
                            columns = homeSettings.folderColumns,
                            rows = homeSettings.folderRows,
                            { gridItem ->
                                val cellWidth = gridWidth / homeSettings.folderColumns

                                val cellHeight =
                                    (gridHeight - pageIndicatorHeightPx - titleHeight) / homeSettings.folderRows

                                val x = gridItem.startColumn * cellWidth

                                val y = gridItem.startRow * cellHeight

                                val width = gridItem.columnSpan * cellWidth

                                val height = gridItem.rowSpan * cellHeight

                                InteractiveGridItemContent(
                                    gridItem = gridItem,
                                    hasShortcutHostPermission = hasShortcutHostPermission,
                                    drag = drag,
                                    gridItemSettings = homeSettings.gridItemSettings,
                                    textColor = textColor,
                                    statusBarNotifications = statusBarNotifications,
                                    isScrollInProgress = folderGridHorizontalPagerState.isScrollInProgress,
                                    iconPackFilePaths = iconPackFilePaths,
                                    onTapApplicationInfo = { serialNumber, componentName ->
                                        val sourceBoundsX = x + leftPadding

                                        val sourceBoundsY = y + topPadding

                                        launcherApps.startMainActivity(
                                            serialNumber = serialNumber,
                                            componentName = componentName,
                                            sourceBounds = Rect(
                                                sourceBoundsX,
                                                sourceBoundsY,
                                                sourceBoundsX + width,
                                                sourceBoundsY + height,
                                            ),
                                        )
                                    },
                                    onTapShortcutInfo = { serialNumber, packageName, shortcutId ->
                                        val sourceBoundsX = x + leftPadding

                                        val sourceBoundsY = y + topPadding

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                                            launcherApps.startShortcut(
                                                serialNumber = serialNumber,
                                                packageName = packageName,
                                                id = shortcutId,
                                                sourceBounds = Rect(
                                                    sourceBoundsX,
                                                    sourceBoundsY,
                                                    sourceBoundsX + width,
                                                    sourceBoundsY + height,
                                                ),
                                            )
                                        }
                                    },
                                    onTapShortcutConfig = { uri ->
                                        context.startActivity(Intent.parseUri(uri, 0))
                                    },
                                    onTapFolderGridItem = {
                                        onAddFolder(gridItem.id)
                                    },
                                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                                    onUpdateImageBitmap = { imageBitmap ->
                                        onLongPressGridItem(
                                            GridItemSource.Existing(gridItem = gridItem),
                                            imageBitmap,
                                        )
                                    },
                                    onResetOverlay = onResetOverlay,
                                    onDraggingGridItem = {
                                        onDraggingGridItem(foldersDataById.last().gridItems)
                                    },
                                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                                )
                            },
                        )
                    }

                    PageIndicator(
                        modifier = Modifier
                            .height(pageIndicatorHeight)
                            .fillMaxWidth(),
                        pageCount = folderGridHorizontalPagerState.pageCount,
                        currentPage = folderGridHorizontalPagerState.currentPage,
                        pageOffset = folderGridHorizontalPagerState.currentPageOffsetFraction,
                        color = getSystemTextColor(textColor = textColor),
                    )
                }
            }
        }
    }
}
