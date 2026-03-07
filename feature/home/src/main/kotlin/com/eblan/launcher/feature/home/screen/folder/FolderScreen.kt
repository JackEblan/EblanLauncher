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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest.Builder
import coil3.request.addLastModifiedToFileCacheKey
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.gesture.onDoubleTap
import com.eblan.launcher.feature.home.component.gesture.swipeGestures
import com.eblan.launcher.feature.home.component.grid.FolderGridLayout
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
import com.eblan.launcher.ui.local.LocalSettings
import kotlinx.coroutines.launch

@Composable
internal fun SharedTransitionScope.FolderScreen(
    drag: Drag,
    folderGridHorizontalPagerState: PagerState,
    folderGridItem: GridItem,
    folderPopupIntOffset: IntOffset,
    folderPopupIntSize: IntSize,
    gridItemSettings: GridItemSettings,
    homeSettings: HomeSettings,
    iconPackFilePaths: Map<String, String>,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    screenHeight: Int,
    screenWidth: Int,
    statusBarNotifications: Map<String, Int>,
    textColor: TextColor,
    onDismissRequest: () -> Unit,
    onDraggingGridItem: () -> Unit,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onOpenAppDrawer: () -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
) {
    val data = folderGridItem.data as? GridItemData.Folder ?: return

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
                        folderGridHeightPx = folderGridHeightPx,
                        folderGridWidthPx = folderGridWidthPx,
                        folderPopupIntOffset = folderPopupIntOffset,
                        folderPopupIntSize = folderPopupIntSize,
                        safeDrawingHeight = safeDrawingHeight,
                        safeDrawingWidth = safeDrawingWidth,
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
                            columns = data.columns,
                            gridItems = data.gridItemsByPage[index],
                            modifier = Modifier.fillMaxSize(),
                            rows = data.rows,
                            { applicationInfoGridItem ->
                                FolderGridItemContent(
                                    drag = drag,
                                    gridItem = applicationInfoGridItem,
                                    gridItemSettings = gridItemSettings,
                                    iconPackFilePaths = iconPackFilePaths,
                                    statusBarNotifications = statusBarNotifications,
                                    textColor = textColor,
                                    onDraggingGridItem = onDraggingGridItem,
                                    onOpenAppDrawer = onOpenAppDrawer,
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
                                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                                )
                            },
                        )
                    }

                    FolderTitle(
                        data = data,
                        folderGridHorizontalPagerState = folderGridHorizontalPagerState,
                        homeSettings = homeSettings,
                        textColor = textColor,
                    )
                }
            },
        )
    }
}

@Composable
internal fun FolderTitle(
    data: GridItemData.Folder,
    folderGridHorizontalPagerState: PagerState,
    homeSettings: HomeSettings,
    modifier: Modifier = Modifier,
    textColor: TextColor,
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
                    systemCustomTextColor = homeSettings.gridItemSettings.customTextColor,
                    systemTextColor = textColor,
                ),
                style = MaterialTheme.typography.bodySmall,
            )

            PageIndicator(
                color = getSystemTextColor(
                    systemCustomTextColor = homeSettings.gridItemSettings.customTextColor,
                    systemTextColor = textColor,
                ),
                gridHorizontalPagerState = folderGridHorizontalPagerState,
                infiniteScroll = false,
                modifier = Modifier.height(PAGE_INDICATOR_HEIGHT),
                pageCount = data.gridItemsByPage.size,
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
                    systemCustomTextColor = homeSettings.gridItemSettings.customTextColor,
                    systemTextColor = textColor,
                ),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun SharedTransitionScope.FolderGridItemContent(
    drag: Drag,
    gridItem: ApplicationInfoGridItem,
    gridItemSettings: GridItemSettings,
    iconPackFilePaths: Map<String, String>,
    modifier: Modifier = Modifier,
    statusBarNotifications: Map<String, Int>,
    textColor: TextColor,
    onDraggingGridItem: () -> Unit,
    onOpenAppDrawer: () -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
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
            gridItemCustomTextColor = gridItem.gridItemSettings.customTextColor,
            gridItemTextColor = gridItem.gridItemSettings.textColor,
            systemCustomTextColor = gridItemSettings.customTextColor,
            systemTextColor = textColor,
        )
    } else {
        getSystemTextColor(
            systemCustomTextColor = gridItemSettings.customTextColor,
            systemTextColor = textColor,
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
            }

            else -> Unit
        }
    }

    Column(
        modifier = modifier
            .pointerInput(key1 = drag) {
                detectTapGestures(
                    onDoubleTap = onDoubleTap(
                        context = context,
                        doubleTap = gridItem.doubleTap,
                        launcherApps = launcherApps,
                        onOpenAppDrawer = onOpenAppDrawer,
                    ),
                    onLongPress = {
                        scope.launch {
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
                    },
                )
            }
            .swipeGestures(
                swipeDown = gridItem.swipeDown,
                swipeUp = gridItem.swipeUp,
                onOpenAppDrawer = onOpenAppDrawer,
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
            val settings = LocalSettings.current
            val maxLines = if (currentGridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE
            val icon = iconPackFilePaths[gridItem.componentName] ?: gridItem.icon
            val hasNotifications =
                statusBarNotifications[gridItem.packageName] != null && (
                    statusBarNotifications[gridItem.packageName]
                        ?: 0
                    ) > 0
            Box(modifier = Modifier.size(currentGridItemSettings.iconSize.dp)) {
                AsyncImage(
                    model = Builder(LocalContext.current).data(gridItem.customIcon ?: icon)
                        .addLastModifiedToFileCacheKey(true).build(),
                    contentDescription = null,
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
                        }.matchParentSize(),
                )

                if (settings.isNotificationAccessGranted() && hasNotifications) {
                    Box(
                        modifier = Modifier
                            .size((currentGridItemSettings.iconSize * 0.3).dp)
                            .align(Alignment.TopEnd)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape,
                            ),
                    )
                }

                if (gridItem.serialNumber != 0L) {
                    ElevatedCard(
                        modifier = Modifier
                            .size((currentGridItemSettings.iconSize * 0.4).dp)
                            .align(Alignment.BottomEnd),
                    ) {
                        Icon(
                            imageVector = EblanLauncherIcons.Work,
                            contentDescription = null,
                            modifier = Modifier.padding(2.dp),
                        )
                    }
                }
            }
            if (currentGridItemSettings.showLabel) {
                Text(
                    text = gridItem.customLabel ?: gridItem.label,
                    color = currentTextColor,
                    textAlign = TextAlign.Center,
                    maxLines = maxLines,
                    fontSize = currentGridItemSettings.textSize.sp,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
