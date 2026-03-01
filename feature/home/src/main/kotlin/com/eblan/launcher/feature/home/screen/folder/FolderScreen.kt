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

import android.graphics.Rect
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.ApplicationInfoFolderGridItemContent
import com.eblan.launcher.feature.home.component.grid.FolderGridLayout
import com.eblan.launcher.feature.home.component.grid.onDoubleTap
import com.eblan.launcher.feature.home.component.grid.swipeGestures
import com.eblan.launcher.feature.home.component.indicator.PageIndicator
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.FolderScreen
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.util.FOLDER_GRID_PADDING
import com.eblan.launcher.feature.home.util.PAGE_INDICATOR_HEIGHT
import com.eblan.launcher.feature.home.util.getGridItemTextColor
import com.eblan.launcher.feature.home.util.getHorizontalAlignment
import com.eblan.launcher.feature.home.util.getSystemTextColor
import com.eblan.launcher.feature.home.util.getVerticalArrangement
import com.eblan.launcher.ui.local.LocalLauncherApps
import kotlinx.coroutines.launch

@Composable
internal fun SharedTransitionScope.FolderScreen(
    modifier: Modifier = Modifier,
    folderGridItem: GridItem,
    folderPopupIntOffset: IntOffset,
    folderPopupIntSize: IntSize,
    paddingValues: PaddingValues,
    folderGridHorizontalPagerState: PagerState,
    screenWidth: Int,
    screenHeight: Int,
    homeSettings: HomeSettings,
    textColor: TextColor,
    gridItemSettings: GridItemSettings,
    statusBarNotifications: Map<String, Int>,
    iconPackFilePaths: Map<String, String>,
    onDismissRequest: () -> Unit,
    drag: Drag,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onDraggingGridItem: () -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onOpenAppDrawer: () -> Unit,
) {
    val data = folderGridItem.data as? GridItemData.Folder ?: error("Expected GridItemData.Folder")

    val density = LocalDensity.current

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

    val folderCellWidth = safeDrawingWidth / homeSettings.columns

    val folderCellHeight = safeDrawingHeight / homeSettings.rows

    val folderGridWidthDp = with(density) {
        (folderCellWidth * data.columns).toDp()
    }

    val folderGridHeightDp = with(density) {
        (folderCellHeight * data.rows).toDp()
    }

    val folderGridWidthPx = with(density) {
        folderGridWidthDp.roundToPx()
    }

    val folderGridHeightPx = with(density) {
        folderGridHeightDp.roundToPx()
    }

    var visible by remember { mutableStateOf(true) }

    val alpha = remember { Animatable(0f) }

    LaunchedEffect(visible) {
        if (visible) {
            alpha.animateTo(1f, animationSpec = tween())
        } else {
            alpha.animateTo(0f, animationSpec = tween())

            onDismissRequest()
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer(alpha = alpha.value)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        awaitRelease()

                        visible = false
                    },
                )
            }
            .fillMaxSize()
            .padding(paddingValues),
    ) {
        Surface(
            modifier = Modifier
                .offset {
                    getFolderScreenOffset(
                        folderPopupIntOffset = folderPopupIntOffset,
                        folderPopupIntSize = folderPopupIntSize,
                        folderGridWidthPx = folderGridWidthPx,
                        folderGridHeightPx = folderGridHeightPx,
                        safeDrawingWidth = safeDrawingWidth,
                        safeDrawingHeight = safeDrawingHeight
                    )
                }
                .size(
                    width = folderGridWidthDp,
                    height = folderGridHeightDp,
                )
                .padding(FOLDER_GRID_PADDING),
            shape = RoundedCornerShape(5.dp),
            shadowElevation = 2.dp,
            content = {
                Column(modifier = Modifier.fillMaxSize()) {
                    HorizontalPager(
                        modifier = Modifier.weight(1f),
                        state = folderGridHorizontalPagerState,
                    ) { index ->
                        FolderGridLayout(
                            modifier = Modifier.fillMaxSize(),
                            gridItems = data.gridItemsByPage[index],
                            columns = data.columns,
                            rows = data.rows,
                            { applicationInfoGridItem ->
                                FolderGridItemContent(
                                    gridItem = applicationInfoGridItem,
                                    textColor = textColor,
                                    gridItemSettings = gridItemSettings,
                                    statusBarNotifications = statusBarNotifications,
                                    iconPackFilePaths = iconPackFilePaths,
                                    drag = drag,
                                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                                    onUpdateImageBitmap = { imageBitmap ->
                                        onLongPressGridItem(
                                            GridItemSource.Folder(
                                                gridItem = folderGridItem,
                                                applicationInfoGridItem = applicationInfoGridItem,
                                            ),
                                            imageBitmap,
                                        )
                                    },
                                    onDraggingGridItem = onDraggingGridItem,
                                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                                    onOpenAppDrawer = onOpenAppDrawer,
                                )
                            },
                        )
                    }

                    FolderTitle(
                        data = data,
                        textColor = textColor,
                        homeSettings = homeSettings,
                        folderGridHorizontalPagerState = folderGridHorizontalPagerState,
                    )
                }
            },
        )
    }
}

@Composable
internal fun FolderTitle(
    modifier: Modifier = Modifier,
    data: GridItemData.Folder,
    textColor: TextColor,
    homeSettings: HomeSettings,
    folderGridHorizontalPagerState: PagerState,
) {
    if (data.gridItemsByPage.size > 1) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = data.label,
                color = getSystemTextColor(
                    systemTextColor = textColor,
                    systemCustomTextColor = homeSettings.gridItemSettings.customTextColor,
                ),
                style = MaterialTheme.typography.bodySmall,
            )

            PageIndicator(
                modifier = Modifier.height(PAGE_INDICATOR_HEIGHT),
                gridHorizontalPagerState = folderGridHorizontalPagerState,
                infiniteScroll = false,
                pageCount = data.gridItemsByPage.size,
                color = getSystemTextColor(
                    systemTextColor = textColor,
                    systemCustomTextColor = homeSettings.gridItemSettings.customTextColor,
                ),
            )
        }
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = data.label,
                color = getSystemTextColor(
                    systemTextColor = textColor,
                    systemCustomTextColor = homeSettings.gridItemSettings.customTextColor,
                ),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun SharedTransitionScope.FolderGridItemContent(
    modifier: Modifier = Modifier,
    gridItem: ApplicationInfoGridItem,
    textColor: TextColor,
    gridItemSettings: GridItemSettings,
    statusBarNotifications: Map<String, Int>,
    iconPackFilePaths: Map<String, String>,
    drag: Drag,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onDraggingGridItem: () -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onOpenAppDrawer: () -> Unit,
) {
    val launcherApps = LocalLauncherApps.current

    val context = LocalContext.current

    val currentGridItemSettings = if (gridItem.override) {
        gridItem.gridItemSettings
    } else {
        gridItemSettings
    }

    val currentTextColor = if (gridItem.override) {
        getGridItemTextColor(
            systemTextColor = textColor,
            systemCustomTextColor = gridItemSettings.customTextColor,
            gridItemTextColor = gridItem.gridItemSettings.textColor,
            gridItemCustomTextColor = gridItem.gridItemSettings.customTextColor,
        )
    } else {
        getSystemTextColor(
            systemTextColor = textColor,
            systemCustomTextColor = gridItemSettings.customTextColor,
        )
    }
    val horizontalAlignment =
        getHorizontalAlignment(horizontalAlignment = currentGridItemSettings.horizontalAlignment)

    val verticalArrangement =
        getVerticalArrangement(verticalArrangement = currentGridItemSettings.verticalArrangement)

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val scale = remember { Animatable(1f) }

    var isLongPress by remember { mutableStateOf(false) }

    val isDragging by remember(key1 = drag) {
        derivedStateOf {
            isLongPress && (drag == Drag.Start || drag == Drag.Dragging)
        }
    }

    LaunchedEffect(key1 = drag) {
        when (drag) {
            Drag.Dragging if isLongPress -> {
                onDraggingGridItem()
            }

            Drag.End, Drag.Cancel -> {
                isLongPress = false

                scale.stop()

                if (scale.value < 1f) {
                    scale.animateTo(1f)
                }
            }

            else -> Unit
        }
    }

    Column(
        modifier = modifier
            .pointerInput(key1 = drag) {
                detectTapGestures(
                    onDoubleTap = onDoubleTap(
                        doubleTap = gridItem.doubleTap,
                        scope = scope,
                        scale = scale,
                        launcherApps = launcherApps,
                        context = context,
                        onOpenAppDrawer = onOpenAppDrawer,
                    ),
                    onLongPress = {
                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            onUpdateImageBitmap(graphicsLayer.toImageBitmap())

                            onUpdateGridItemOffset(
                                intOffset,
                                intSize,
                            )

                            onUpdateSharedElementKey(
                                SharedElementKey(
                                    id = gridItem.id,
                                    screen = FolderScreen.Folder,
                                ),
                            )

                            isLongPress = true
                        }
                    },
                    onTap = {
                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            launcherApps.startMainActivity(
                                serialNumber = gridItem.serialNumber,
                                componentName = gridItem.componentName,
                                sourceBounds = Rect(
                                    intOffset.x,
                                    intOffset.y,
                                    intOffset.x + intSize.width,
                                    intOffset.y + intSize.height,
                                ),
                            )
                        }
                    },
                    onPress = {
                        awaitRelease()

                        scale.stop()

                        if (scale.value < 1f) {
                            scale.animateTo(1f)
                        }
                    },
                )
            }
            .swipeGestures(
                swipeUp = gridItem.swipeUp,
                swipeDown = gridItem.swipeDown,
                onOpenAppDrawer = onOpenAppDrawer,
            )
            .scale(
                scaleX = scale.value,
                scaleY = scale.value,
            )
            .fillMaxSize()
            .padding(currentGridItemSettings.padding.dp)
            .background(
                color = Color(currentGridItemSettings.customBackgroundColor),
                shape = RoundedCornerShape(size = currentGridItemSettings.cornerRadius.dp),
            ),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        if (!isDragging) {
            ApplicationInfoFolderGridItemContent(
                modifier = Modifier
                    .sharedElementWithCallerManagedVisibility(
                        rememberSharedContentState(
                            key = SharedElementKey(
                                id = gridItem.id,
                                screen = FolderScreen.Folder,
                            ),
                        ),
                        visible = drag == Drag.Cancel || drag == Drag.End,
                    )
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }

                        drawLayer(graphicsLayer)
                    }
                    .onGloballyPositioned { layoutCoordinates ->
                        intOffset = layoutCoordinates.positionInRoot().round()

                        intSize = layoutCoordinates.size
                    },
                gridItem = gridItem,
                textColor = currentTextColor,
                gridItemSettings = currentGridItemSettings,
                statusBarNotifications = statusBarNotifications,
                iconPackFilePaths = iconPackFilePaths,
            )
        }
    }
}
