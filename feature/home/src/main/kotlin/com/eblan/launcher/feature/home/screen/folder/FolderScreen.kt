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
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.util.Consumer
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfoGroup
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.model.EblanShortcutInfoByGroup
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
import com.eblan.launcher.feature.home.screen.pager.GridItemPopup
import com.eblan.launcher.feature.home.screen.widget.AppWidgetScreen
import com.eblan.launcher.feature.home.util.PAGE_INDICATOR_HEIGHT
import com.eblan.launcher.feature.home.util.getSystemTextColor
import com.eblan.launcher.feature.home.util.handleActionMainIntent
import com.eblan.launcher.ui.local.LocalLauncherApps
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.FolderScreen(
    modifier: Modifier = Modifier,
    folderGridHorizontalPagerState: PagerState,
    folderDataById: FolderDataById,
    drag: Drag,
    paddingValues: PaddingValues,
    hasShortcutHostPermission: Boolean,
    screenWidth: Int,
    screenHeight: Int,
    textColor: TextColor,
    homeSettings: HomeSettings,
    statusBarNotifications: Map<String, Int>,
    iconPackFilePaths: Map<String, String>,
    screen: Screen,
    gridItemSource: GridItemSource?,
    eblanShortcutInfosGroup: Map<EblanShortcutInfoByGroup, List<EblanShortcutInfo>>,
    eblanAppWidgetProviderInfosGroup: Map<String, List<EblanAppWidgetProviderInfo>>,
    gridItems: List<GridItem>,
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
    onDraggingFolderGridItem: (
        screen: Screen,
        gridItems: List<GridItem>,
    ) -> Unit,
    onDraggingGridItem: (
        screen: Screen,
        gridItems: List<GridItem>,
    ) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onResetOverlay: () -> Unit,
    onResize: (
        screen: Screen,
        gridItems: List<GridItem>,
    ) -> Unit,
    onEditGridItem: (String) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
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

    val safeDrawingWidth = screenWidth - horizontalPadding

    val safeDrawingHeight = screenHeight - verticalPadding

    var titleHeight by remember { mutableIntStateOf(0) }

    val activity = LocalActivity.current as ComponentActivity

    val scope = rememberCoroutineScope()

    val pageIndicatorHeightPx = with(density) {
        PAGE_INDICATOR_HEIGHT.dp.roundToPx()
    }

    var showGridItemPopup by remember { mutableStateOf(false) }

    var popupIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var popupIntSize by remember { mutableStateOf(IntSize.Zero) }

    var eblanApplicationInfoGroup by remember { mutableStateOf<EblanApplicationInfoGroup?>(null) }

    var isPressHome by remember { mutableStateOf(false) }

    DisposableEffect(key1 = activity) {
        val listener = Consumer<Intent> { intent ->
            scope.launch {
                handleActionMainIntent(
                    intent = intent,
                    onActionMainIntent = {
                        showGridItemPopup = false

                        isPressHome = true

                        onUpdateScreen(Screen.Pager)
                    },
                )
            }
        }

        activity.addOnNewIntentListener(listener)

        onDispose {
            activity.removeOnNewIntentListener(listener)
        }
    }

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.End || drag == Drag.Cancel) {
            onResetOverlay()
        }
    }

    BackHandler {
        showGridItemPopup = false

        onRemoveLastFolder()
    }

    Column(
        modifier = modifier
            .pointerInput(key1 = Unit) {
                detectTapGestures(
                    onTap = {
                        onUpdateScreen(Screen.Pager)
                    },
                )
            }
            .fillMaxSize()
            .padding(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding(),
            ),
    ) {
        Column(
            modifier = Modifier
                .onSizeChanged {
                    titleHeight = it.height
                }
                .fillMaxWidth()
                .padding(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = folderDataById.label,
                color = getSystemTextColor(
                    systemTextColor = textColor,
                    systemCustomTextColor = homeSettings.gridItemSettings.customTextColor,
                ),
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
                gridItems = folderDataById.gridItemsByPage[index],
                columns = homeSettings.folderColumns,
                rows = homeSettings.folderRows,
                { gridItem ->
                    val cellWidth = safeDrawingWidth / homeSettings.folderColumns

                    val cellHeight =
                        (safeDrawingHeight - pageIndicatorHeightPx - titleHeight) / homeSettings.folderRows

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
                        screen = screen,
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
                        onUpdateGridItemOffset = { intOffset, intSize ->
                            popupIntOffset = intOffset

                            popupIntSize = IntSize(
                                width = intSize.width,
                                height = height,
                            )

                            onUpdateGridItemOffset(intOffset, intSize)

                            showGridItemPopup = true
                        },
                        onUpdateImageBitmap = { imageBitmap ->
                            onLongPressGridItem(
                                GridItemSource.Existing(gridItem = gridItem),
                                imageBitmap,
                            )
                        },
                        onDraggingGridItem = {
                            showGridItemPopup = false

                            onDraggingFolderGridItem(
                                Screen.FolderDrag(folderDataById = folderDataById),
                                folderDataById.gridItems,
                            )
                        },
                        onUpdateSharedElementKey = onUpdateSharedElementKey,
                        onOpenAppDrawer = {
                            onUpdateScreen(Screen.Pager)
                        },
                    )
                },
            )
        }

        PageIndicator(
            modifier = Modifier
                .height(PAGE_INDICATOR_HEIGHT.dp)
                .fillMaxWidth(),
            gridHorizontalPagerState = folderGridHorizontalPagerState,
            infiniteScroll = false,
            pageCount = folderDataById.pageCount,
            color = getSystemTextColor(
                systemTextColor = textColor,
                systemCustomTextColor = homeSettings.gridItemSettings.customTextColor,
            ),
        )
    }

    if (showGridItemPopup && gridItemSource?.gridItem != null) {
        GridItemPopup(
            gridItem = gridItemSource.gridItem,
            popupIntOffset = popupIntOffset,
            popupIntSize = popupIntSize,
            eblanShortcutInfosGroup = eblanShortcutInfosGroup,
            hasShortcutHostPermission = hasShortcutHostPermission,
            currentPage = folderGridHorizontalPagerState.currentPage,
            drag = drag,
            gridItemSettings = homeSettings.gridItemSettings,
            eblanAppWidgetProviderInfosGroup = eblanAppWidgetProviderInfosGroup,
            paddingValues = paddingValues,
            onEdit = { id ->
                onUpdateScreen(Screen.Pager)

                onEditGridItem(id)
            },
            onResize = {
                onResize(
                    Screen.FolderResize(folderDataById = folderDataById),
                    folderDataById.gridItems,
                )
            },
            onWidgets = { newEblanApplicationInfoGroup ->
                eblanApplicationInfoGroup = newEblanApplicationInfoGroup
            },
            onDeleteGridItem = { gridItem ->
                onUpdateScreen(Screen.Pager)

                onDeleteGridItem(gridItem)
            },
            onInfo = { serialNumber, componentName ->
                launcherApps.startAppDetailsActivity(
                    serialNumber = serialNumber,
                    componentName = componentName,
                    sourceBounds = Rect(
                        popupIntOffset.x,
                        popupIntOffset.y,
                        popupIntOffset.x + popupIntSize.width,
                        popupIntOffset.y + popupIntSize.height,
                    ),
                )
            },
            onDismissRequest = {
                showGridItemPopup = false
            },
            onTapShortcutInfo = { serialNumber, packageName, shortcutId ->
                val sourceBoundsX = popupIntOffset.x + leftPadding

                val sourceBoundsY = popupIntOffset.y + topPadding

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    launcherApps.startShortcut(
                        serialNumber = serialNumber,
                        packageName = packageName,
                        id = shortcutId,
                        sourceBounds = Rect(
                            sourceBoundsX,
                            sourceBoundsY,
                            sourceBoundsX + popupIntSize.width,
                            sourceBoundsY + popupIntSize.height,
                        ),
                    )
                }
            },
            onLongPressGridItem = onLongPressGridItem,
            onUpdateGridItemOffset = onUpdateGridItemOffset,
            onDraggingGridItem = {
                onDraggingGridItem(
                    Screen.FolderDrag(folderDataById = folderDataById),
                    folderDataById.gridItems,
                )
            },
            onUpdateSharedElementKey = onUpdateSharedElementKey,
        )
    }

    if (eblanApplicationInfoGroup != null) {
        AppWidgetScreen(
            currentPage = folderGridHorizontalPagerState.currentPage,
            eblanApplicationInfoGroup = eblanApplicationInfoGroup,
            eblanAppWidgetProviderInfosGroup = eblanAppWidgetProviderInfosGroup,
            gridItemSettings = homeSettings.gridItemSettings,
            paddingValues = paddingValues,
            drag = drag,
            isPressHome = isPressHome,
            screen = screen,
            gridItems = gridItems,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            columns = homeSettings.columns,
            rows = homeSettings.rows,
            onLongPressGridItem = onLongPressGridItem,
            onUpdateGridItemOffset = onUpdateGridItemOffset,
            onDismiss = {
                eblanApplicationInfoGroup = null

                isPressHome = false
            },
            onDraggingGridItem = onDraggingGridItem,
            onUpdateSharedElementKey = onUpdateSharedElementKey,
        )
    }
}
