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
package com.eblan.launcher.feature.home.screen.application

import android.graphics.Rect
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.addLastModifiedToFileCacheKey
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.model.AppDrawerSettings
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.EblanApplicationInfoGroup
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.model.EblanShortcutInfoByGroup
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.HorizontalAlignment
import com.eblan.launcher.domain.model.VerticalArrangement
import com.eblan.launcher.feature.home.component.scroll.OffsetNestedScrollConnection
import com.eblan.launcher.feature.home.component.scroll.OffsetOverscrollEffect
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.EblanApplicationComponentUiState
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.screen.loading.LoadingScreen
import com.eblan.launcher.feature.home.screen.widget.AppWidgetScreen
import com.eblan.launcher.feature.home.util.getSystemTextColor
import com.eblan.launcher.ui.local.LocalLauncherApps
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.ApplicationScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    offsetY: () -> Float,
    eblanApplicationComponentUiState: EblanApplicationComponentUiState,
    paddingValues: PaddingValues,
    drag: Drag,
    appDrawerSettings: AppDrawerSettings,
    eblanApplicationInfosByLabel: List<EblanApplicationInfo>,
    gridItemSource: GridItemSource?,
    iconPackInfoPackageName: String,
    screenHeight: Int,
    eblanShortcutInfos: Map<EblanShortcutInfoByGroup, List<EblanShortcutInfo>>,
    hasShortcutHostPermission: Boolean,
    eblanAppWidgetProviderInfos: Map<String, List<EblanAppWidgetProviderInfo>>,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onGetEblanApplicationInfosByLabel: (String) -> Unit,
    onDismiss: () -> Unit,
    onDraggingGridItem: () -> Unit,
    onResetOverlay: () -> Unit,
    onVerticalDrag: (Float) -> Unit,
    onDragEnd: (Float) -> Unit,
    onEditApplicationInfo: (
        serialNumber: Long,
        packageName: String,
    ) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
) {
    val alpha by remember {
        derivedStateOf {
            ((screenHeight - offsetY()) / (screenHeight / 2)).coerceIn(0f, 1f)
        }
    }

    val cornerSize by remember {
        derivedStateOf {
            val progress = offsetY().coerceAtLeast(0f) / screenHeight

            (20 * progress).dp
        }
    }

    Surface(
        modifier = modifier
            .offset {
                IntOffset(x = 0, y = offsetY().roundToInt())
            }
            .fillMaxSize()
            .clip(RoundedCornerShape(cornerSize))
            .alpha(alpha),
    ) {
        when (eblanApplicationComponentUiState) {
            EblanApplicationComponentUiState.Loading -> {
                LoadingScreen()
            }

            is EblanApplicationComponentUiState.Success -> {
                Success(
                    currentPage = currentPage,
                    paddingValues = paddingValues,
                    drag = drag,
                    appDrawerSettings = appDrawerSettings,
                    eblanApplicationInfosByLabel = eblanApplicationInfosByLabel,
                    gridItemSource = gridItemSource,
                    iconPackInfoPackageName = iconPackInfoPackageName,
                    eblanApplicationInfos = eblanApplicationComponentUiState.eblanApplicationComponent.eblanApplicationInfos,
                    eblanShortcutInfos = eblanShortcutInfos,
                    hasShortcutHostPermission = hasShortcutHostPermission,
                    screenHeight = screenHeight,
                    eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
                    onLongPressGridItem = onLongPressGridItem,
                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                    onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
                    onDismiss = onDismiss,
                    onDraggingGridItem = onDraggingGridItem,
                    onResetOverlay = onResetOverlay,
                    onVerticalDrag = onVerticalDrag,
                    onDragEnd = onDragEnd,
                    onEditApplicationInfo = onEditApplicationInfo,
                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.Success(
    modifier: Modifier = Modifier,
    currentPage: Int,
    paddingValues: PaddingValues,
    drag: Drag,
    appDrawerSettings: AppDrawerSettings,
    eblanApplicationInfosByLabel: List<EblanApplicationInfo>,
    gridItemSource: GridItemSource?,
    iconPackInfoPackageName: String,
    eblanApplicationInfos: Map<Long, List<EblanApplicationInfo>>,
    eblanShortcutInfos: Map<EblanShortcutInfoByGroup, List<EblanShortcutInfo>>,
    hasShortcutHostPermission: Boolean,
    screenHeight: Int,
    eblanAppWidgetProviderInfos: Map<String, List<EblanAppWidgetProviderInfo>>,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onGetEblanApplicationInfosByLabel: (String) -> Unit,
    onDismiss: () -> Unit,
    onDraggingGridItem: () -> Unit,
    onResetOverlay: () -> Unit,
    onVerticalDrag: (Float) -> Unit,
    onDragEnd: (Float) -> Unit,
    onEditApplicationInfo: (
        serialNumber: Long,
        packageName: String,
    ) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
) {
    val density = LocalDensity.current

    val focusManager = LocalFocusManager.current

    var showPopupApplicationMenu by remember { mutableStateOf(false) }

    var popupIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var popupIntSize by remember { mutableStateOf(IntSize.Zero) }

    val launcherApps = LocalLauncherApps.current

    val leftPadding = with(density) {
        paddingValues.calculateStartPadding(LayoutDirection.Ltr).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val horizontalPagerState = rememberPagerState(
        pageCount = {
            eblanApplicationInfos.keys.size
        },
    )

    val appDrawerRowsHeight = with(density) {
        appDrawerSettings.appDrawerRowsHeight.dp.roundToPx()
    }

    var eblanApplicationInfoGroup by remember { mutableStateOf<EblanApplicationInfoGroup?>(null) }

    BackHandler {
        showPopupApplicationMenu = false

        onDismiss()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                top = paddingValues.calculateTopPadding(),
                start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
            ),
    ) {
        EblanApplicationInfoDockSearchBar(
            currentPage = currentPage,
            onQueryChange = onGetEblanApplicationInfosByLabel,
            eblanApplicationInfosByLabel = eblanApplicationInfosByLabel,
            drag = drag,
            appDrawerSettings = appDrawerSettings,
            iconPackInfoPackageName = iconPackInfoPackageName,
            paddingValues = paddingValues,
            onUpdateGridItemOffset = { intOffset, intSize ->
                onUpdateGridItemOffset(intOffset, intSize)

                popupIntOffset = intOffset

                popupIntSize = intSize

                focusManager.clearFocus()
            },
            onLongPressGridItem = onLongPressGridItem,
            onUpdatePopupMenu = { newShowPopupApplicationMenu ->
                showPopupApplicationMenu = newShowPopupApplicationMenu
            },
            onResetOverlay = onResetOverlay,
            onDraggingGridItem = onDraggingGridItem,
            onUpdateSharedElementKey = onUpdateSharedElementKey,
        )

        if (eblanApplicationInfos.keys.size > 1) {
            EblanApplicationInfoTabRow(
                currentPage = horizontalPagerState.currentPage,
                eblanApplicationInfos = eblanApplicationInfos,
                onAnimateScrollToPage = horizontalPagerState::animateScrollToPage,
            )

            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = horizontalPagerState,
            ) { index ->
                EblanApplicationInfosPage(
                    index = index,
                    currentPage = currentPage,
                    paddingValues = paddingValues,
                    drag = drag,
                    appDrawerSettings = appDrawerSettings,
                    iconPackInfoPackageName = iconPackInfoPackageName,
                    eblanApplicationInfos = eblanApplicationInfos,
                    onLongPressGridItem = onLongPressGridItem,
                    onResetOverlay = onResetOverlay,
                    onUpdateGridItemOffset = { intOffset, intSize ->
                        onUpdateGridItemOffset(intOffset, intSize)

                        popupIntOffset = intOffset

                        popupIntSize = intSize
                    },
                    onUpdatePopupMenu = { newShowPopupApplicationMenu ->
                        showPopupApplicationMenu = newShowPopupApplicationMenu
                    },
                    onVerticalDrag = onVerticalDrag,
                    onDragEnd = onDragEnd,
                    onDraggingGridItem = onDraggingGridItem,
                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                )
            }
        } else {
            EblanApplicationInfosPage(
                index = 0,
                currentPage = currentPage,
                paddingValues = paddingValues,
                drag = drag,
                appDrawerSettings = appDrawerSettings,
                iconPackInfoPackageName = iconPackInfoPackageName,
                eblanApplicationInfos = eblanApplicationInfos,
                onLongPressGridItem = onLongPressGridItem,
                onResetOverlay = onResetOverlay,
                onUpdateGridItemOffset = { intOffset, intSize ->
                    onUpdateGridItemOffset(intOffset, intSize)

                    popupIntOffset = intOffset

                    popupIntSize = IntSize(
                        width = intSize.width,
                        height = appDrawerRowsHeight,
                    )
                },
                onUpdatePopupMenu = { newShowPopupApplicationMenu ->
                    showPopupApplicationMenu = newShowPopupApplicationMenu
                },
                onVerticalDrag = onVerticalDrag,
                onDragEnd = onDragEnd,
                onDraggingGridItem = onDraggingGridItem,
                onUpdateSharedElementKey = onUpdateSharedElementKey,
            )
        }
    }

    if (showPopupApplicationMenu && gridItemSource?.gridItem != null) {
        PopupApplicationInfoMenu(
            paddingValues = paddingValues,
            popupIntOffset = popupIntOffset,
            gridItem = gridItemSource.gridItem,
            popupIntSize = popupIntSize,
            eblanShortcutInfos = eblanShortcutInfos,
            hasShortcutHostPermission = hasShortcutHostPermission,
            currentPage = currentPage,
            drag = drag,
            gridItemSettings = appDrawerSettings.gridItemSettings,
            eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
            onDismissRequest = {
                showPopupApplicationMenu = false
            },
            onEditApplicationInfo = onEditApplicationInfo,
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
            onResetOverlay = onResetOverlay,
            onLongPressGridItem = onLongPressGridItem,
            onUpdateGridItemOffset = onUpdateGridItemOffset,
            onDraggingGridItem = onDraggingGridItem,
            onWidgets = { newEblanApplicationInfoGroup ->
                eblanApplicationInfoGroup = newEblanApplicationInfoGroup
            },
            onUpdateSharedElementKey = onUpdateSharedElementKey,
        )
    }

    if (eblanApplicationInfoGroup != null) {
        AppWidgetScreen(
            currentPage = currentPage,
            eblanApplicationInfoGroup = eblanApplicationInfoGroup,
            eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
            gridItemSettings = appDrawerSettings.gridItemSettings,
            paddingValues = paddingValues,
            drag = drag,
            screenHeight = screenHeight,
            onLongPressGridItem = onLongPressGridItem,
            onUpdateGridItemOffset = onUpdateGridItemOffset,
            onDismiss = {
                eblanApplicationInfoGroup = null
            },
            onDraggingGridItem = onDraggingGridItem,
            onResetOverlay = onResetOverlay,
            onUpdateSharedElementKey = onUpdateSharedElementKey,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.EblanApplicationInfoDockSearchBar(
    modifier: Modifier = Modifier,
    currentPage: Int,
    drag: Drag,
    appDrawerSettings: AppDrawerSettings,
    onQueryChange: (String) -> Unit,
    eblanApplicationInfosByLabel: List<EblanApplicationInfo>,
    iconPackInfoPackageName: String,
    paddingValues: PaddingValues,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdatePopupMenu: (Boolean) -> Unit,
    onResetOverlay: () -> Unit,
    onDraggingGridItem: () -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
) {
    var query by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }

    DockedSearchBar(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp),
        inputField = {
            SearchBarDefaults.InputField(
                modifier = Modifier.fillMaxWidth(),
                query = query,
                onQueryChange = { newQuery ->
                    query = newQuery

                    onQueryChange(newQuery)
                },
                onSearch = { expanded = false },
                expanded = expanded,
                onExpandedChange = { expanded = it },
                placeholder = { Text("Search Applications") },
                leadingIcon = { Icon(EblanLauncherIcons.Search, contentDescription = null) },
            )
        },
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(count = appDrawerSettings.appDrawerColumns),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(eblanApplicationInfosByLabel) { eblanApplicationInfo ->
                EblanApplicationInfoItem(
                    currentPage = currentPage,
                    drag = drag,
                    eblanApplicationInfo = eblanApplicationInfo,
                    appDrawerSettings = appDrawerSettings,
                    iconPackInfoPackageName = iconPackInfoPackageName,
                    paddingValues = paddingValues,
                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                    onLongPressGridItem = onLongPressGridItem,
                    onUpdatePopupMenu = onUpdatePopupMenu,
                    onResetOverlay = onResetOverlay,
                    onDraggingGridItem = onDraggingGridItem,
                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                )
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.EblanApplicationInfoItem(
    modifier: Modifier = Modifier,
    currentPage: Int,
    drag: Drag,
    eblanApplicationInfo: EblanApplicationInfo,
    appDrawerSettings: AppDrawerSettings,
    iconPackInfoPackageName: String,
    paddingValues: PaddingValues,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdatePopupMenu: (Boolean) -> Unit,
    onResetOverlay: () -> Unit,
    onDraggingGridItem: () -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
) {
    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val graphicsLayer = rememberGraphicsLayer()

    val scale = remember { Animatable(1f) }

    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    val density = LocalDensity.current

    val launcherApps = LocalLauncherApps.current

    val textColor = getSystemTextColor(textColor = appDrawerSettings.gridItemSettings.textColor)

    val appDrawerRowsHeight = appDrawerSettings.appDrawerRowsHeight.dp

    val maxLines = if (appDrawerSettings.gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    val iconPacksDirectory = File(context.filesDir, FileManager.ICON_PACKS_DIR)

    val iconPackDirectory = File(iconPacksDirectory, iconPackInfoPackageName)

    val iconFile = File(
        iconPackDirectory,
        eblanApplicationInfo.componentName.replace("/", "-"),
    )

    val icon = if (iconPackInfoPackageName.isNotEmpty() && iconFile.exists()) {
        iconFile.absolutePath
    } else {
        eblanApplicationInfo.icon
    }

    val horizontalAlignment = when (appDrawerSettings.gridItemSettings.horizontalAlignment) {
        HorizontalAlignment.Start -> Alignment.Start
        HorizontalAlignment.CenterHorizontally -> Alignment.CenterHorizontally
        HorizontalAlignment.End -> Alignment.End
    }

    val verticalArrangement = when (appDrawerSettings.gridItemSettings.verticalArrangement) {
        VerticalArrangement.Top -> Arrangement.Top
        VerticalArrangement.Center -> Arrangement.Center
        VerticalArrangement.Bottom -> Arrangement.Bottom
    }

    val leftPadding = with(density) {
        paddingValues.calculateStartPadding(LayoutDirection.Ltr).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val customIcon = eblanApplicationInfo.customIcon ?: icon

    val customLabel = eblanApplicationInfo.customLabel ?: eblanApplicationInfo.label

    var isLongPress by remember { mutableStateOf(false) }

    val isDragging = isLongPress && (drag == Drag.Start || drag == Drag.Dragging)

    val id = remember { Uuid.random().toHexString() }

    LaunchedEffect(key1 = drag) {
        when (drag) {
            Drag.Dragging -> {
                if (isLongPress) {
                    onUpdateSharedElementKey(
                        SharedElementKey(
                            id = id,
                            screen = Screen.Drag,
                        ),
                    )

                    onDraggingGridItem()

                    onUpdatePopupMenu(false)
                }
            }

            Drag.End, Drag.Cancel -> {
                isLongPress = false

                scale.stop()

                if (scale.value < 1f) {
                    scale.animateTo(1f)
                }

                onResetOverlay()
            }

            else -> Unit
        }
    }

    Column(
        modifier = modifier
            .pointerInput(key1 = drag) {
                detectTapGestures(
                    onTap = {
                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            val sourceBoundsX = intOffset.x + leftPadding

                            val sourceBoundsY = intOffset.y + topPadding

                            launcherApps.startMainActivity(
                                serialNumber = eblanApplicationInfo.serialNumber,
                                componentName = eblanApplicationInfo.componentName,
                                sourceBounds = Rect(
                                    sourceBoundsX,
                                    sourceBoundsY,
                                    sourceBoundsX + intSize.width,
                                    sourceBoundsY + intSize.height,
                                ),
                            )
                        }
                    },
                    onLongPress = {
                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            val data = GridItemData.ApplicationInfo(
                                serialNumber = eblanApplicationInfo.serialNumber,
                                componentName = eblanApplicationInfo.componentName,
                                packageName = eblanApplicationInfo.packageName,
                                icon = eblanApplicationInfo.icon,
                                label = eblanApplicationInfo.label,
                                customIcon = eblanApplicationInfo.customIcon,
                                customLabel = eblanApplicationInfo.customLabel,
                            )

                            onLongPressGridItem(
                                GridItemSource.New(
                                    gridItem = GridItem(
                                        id = id,
                                        folderId = null,
                                        page = currentPage,
                                        startColumn = -1,
                                        startRow = -1,
                                        columnSpan = 1,
                                        rowSpan = 1,
                                        data = data,
                                        associate = Associate.Grid,
                                        override = false,
                                        gridItemSettings = appDrawerSettings.gridItemSettings,
                                    ),
                                ),
                                graphicsLayer.toImageBitmap(),
                            )

                            onUpdateGridItemOffset(
                                intOffset,
                                intSize,
                            )

                            onUpdateSharedElementKey(
                                SharedElementKey(
                                    id = id,
                                    screen = Screen.Pager,
                                ),
                            )

                            onUpdatePopupMenu(true)

                            isLongPress = true
                        }
                    },
                    onPress = {
                        awaitRelease()

                        scale.stop()

                        isLongPress = false

                        onResetOverlay()

                        if (scale.value < 1f) {
                            scale.animateTo(1f)
                        }
                    },
                )
            }
            .height(appDrawerRowsHeight)
            .scale(
                scaleX = scale.value,
                scaleY = scale.value,
            ),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        if (!isDragging) {
            Box(
                modifier = Modifier.size(appDrawerSettings.gridItemSettings.iconSize.dp),
            ) {
                AsyncImage(
                    model = ImageRequest
                        .Builder(context)
                        .data(customIcon)
                        .addLastModifiedToFileCacheKey(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .sharedElementWithCallerManagedVisibility(
                            rememberSharedContentState(
                                key = SharedElementKey(
                                    id = id,
                                    screen = Screen.Pager,
                                ),
                            ),
                            visible = true,
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
                        }
                        .matchParentSize(),
                )

                if (eblanApplicationInfo.serialNumber != 0L) {
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
            }

            if (appDrawerSettings.gridItemSettings.showLabel) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = customLabel,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    maxLines = maxLines,
                    fontSize = appDrawerSettings.gridItemSettings.textSize.sp,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.EblanApplicationInfosPage(
    modifier: Modifier = Modifier,
    index: Int,
    currentPage: Int,
    paddingValues: PaddingValues,
    drag: Drag,
    appDrawerSettings: AppDrawerSettings,
    iconPackInfoPackageName: String,
    eblanApplicationInfos: Map<Long, List<EblanApplicationInfo>>,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onResetOverlay: () -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdatePopupMenu: (Boolean) -> Unit,
    onVerticalDrag: (Float) -> Unit,
    onDragEnd: (Float) -> Unit,
    onDraggingGridItem: () -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
) {
    val scope = rememberCoroutineScope()

    val overscrollEffect = remember(key1 = scope) {
        OffsetOverscrollEffect(
            scope = scope,
            onVerticalDrag = onVerticalDrag,
            onDragEnd = onDragEnd,
        )
    }

    val lazyGridState = rememberLazyGridState()

    val serialNumber = eblanApplicationInfos.keys.toList().getOrElse(
        index = index,
        defaultValue = {
            0
        },
    )

    val canOverscroll by remember(key1 = lazyGridState) {
        derivedStateOf {
            val lastVisibleIndex =
                lazyGridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            lastVisibleIndex < lazyGridState.layoutInfo.totalItemsCount - 1
        }
    }

    val nestedScrollConnection = remember {
        OffsetNestedScrollConnection(
            onVerticalDrag = onVerticalDrag,
            onDragEnd = onDragEnd,
        )
    }

    Box(
        modifier = modifier
            .run {
                if (!canOverscroll) {
                    nestedScroll(nestedScrollConnection)
                } else {
                    this
                }
            }
            .fillMaxSize(),
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(count = appDrawerSettings.appDrawerColumns),
            state = lazyGridState,
            modifier = Modifier.matchParentSize(),
            contentPadding = PaddingValues(
                bottom = paddingValues.calculateBottomPadding(),
            ),
            overscrollEffect = if (canOverscroll) {
                overscrollEffect
            } else {
                rememberOverscrollEffect()
            },
        ) {
            items(eblanApplicationInfos[serialNumber].orEmpty()) { eblanApplicationInfo ->
                EblanApplicationInfoItem(
                    currentPage = currentPage,
                    drag = drag,
                    eblanApplicationInfo = eblanApplicationInfo,
                    appDrawerSettings = appDrawerSettings,
                    iconPackInfoPackageName = iconPackInfoPackageName,
                    paddingValues = paddingValues,
                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                    onLongPressGridItem = onLongPressGridItem,
                    onUpdatePopupMenu = onUpdatePopupMenu,
                    onResetOverlay = onResetOverlay,
                    onDraggingGridItem = onDraggingGridItem,
                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                )
            }
        }

        if (!WindowInsets.isImeVisible) {
            ScrollBarThumb(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .fillMaxHeight(),
                lazyGridState = lazyGridState,
                appDrawerSettings = appDrawerSettings,
                paddingValues = paddingValues,
                eblanApplicationInfos = eblanApplicationInfos[serialNumber].orEmpty(),
                onScrollToItem = lazyGridState::scrollToItem,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun EblanApplicationInfoTabRow(
    currentPage: Int,
    eblanApplicationInfos: Map<Long, List<EblanApplicationInfo>>,
    onAnimateScrollToPage: suspend (Int) -> Unit,
) {
    val scope = rememberCoroutineScope()

    SecondaryTabRow(selectedTabIndex = currentPage) {
        eblanApplicationInfos.keys.forEachIndexed { index, serialNumber ->
            Tab(
                selected = currentPage == index,
                onClick = {
                    scope.launch {
                        onAnimateScrollToPage(index)
                    }
                },
                text = {
                    Text(
                        text = "User $serialNumber",
                        maxLines = 1,
                    )
                },
            )
        }
    }
}

@Composable
private fun ScrollBarThumb(
    modifier: Modifier = Modifier,
    lazyGridState: LazyGridState,
    appDrawerSettings: AppDrawerSettings,
    paddingValues: PaddingValues,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    onScrollToItem: suspend (Int) -> Unit,
) {
    val density = LocalDensity.current

    val scope = rememberCoroutineScope()

    val appDrawerRowsHeightPx = with(density) {
        appDrawerSettings.appDrawerRowsHeight.dp.roundToPx()
    }

    val bottomPadding = with(density) {
        paddingValues.calculateBottomPadding().roundToPx()
    }

    val thumbHeight by remember(lazyGridState) {
        derivedStateOf {
            with(density) {
                (lazyGridState.layoutInfo.viewportSize.height / 4).toDp()
            }
        }
    }

    val viewPortThumbY by remember(key1 = lazyGridState) {
        derivedStateOf {
            val totalRows =
                (lazyGridState.layoutInfo.totalItemsCount + appDrawerSettings.appDrawerColumns - 1) / appDrawerSettings.appDrawerColumns

            val visibleRows =
                ceil(lazyGridState.layoutInfo.viewportSize.height / appDrawerRowsHeightPx.toFloat()).toInt()

            val scrollableRows = (totalRows - visibleRows).coerceAtLeast(0)

            val availableScroll = scrollableRows * appDrawerRowsHeightPx

            val row = lazyGridState.firstVisibleItemIndex / appDrawerSettings.appDrawerColumns

            val totalScrollY =
                (row * appDrawerRowsHeightPx) + lazyGridState.firstVisibleItemScrollOffset

            val thumbHeightPx = with(density) {
                thumbHeight.toPx()
            }

            val availableHeight =
                (lazyGridState.layoutInfo.viewportSize.height - thumbHeightPx - bottomPadding).coerceAtLeast(
                    0f,
                )

            if (availableScroll <= 0) {
                0f
            } else {
                (totalScrollY.toFloat() / availableScroll.toFloat() * availableHeight).coerceIn(
                    0f,
                    availableHeight,
                )
            }
        }
    }

    var isDraggingThumb by remember { mutableStateOf(false) }

    var thumbY by remember { mutableFloatStateOf(0f) }

    val thumbAlpha by animateFloatAsState(
        targetValue = if (lazyGridState.isScrollInProgress || isDraggingThumb) 1f else 0.2f,
    )

    val firstVisibleItem by remember(key1 = lazyGridState, key2 = eblanApplicationInfos) {
        derivedStateOf {
            if (isDraggingThumb && lazyGridState.firstVisibleItemIndex in eblanApplicationInfos.indices) {
                eblanApplicationInfos[lazyGridState.firstVisibleItemIndex].label
            } else {
                null
            }
        }
    }

    Row(modifier = modifier) {
        if (isDraggingThumb) {
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(x = 0, y = thumbY.roundToInt())
                    }
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    modifier = Modifier.padding(10.dp),
                    text = firstVisibleItem.orEmpty(),
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }

        Box(
            modifier = Modifier
                .offset {
                    val y = if (isDraggingThumb) {
                        thumbY
                    } else {
                        viewPortThumbY
                    }

                    IntOffset(0, y.roundToInt())
                }
                .pointerInput(key1 = lazyGridState) {
                    detectVerticalDragGestures(
                        onDragStart = {
                            thumbY = viewPortThumbY

                            isDraggingThumb = true
                        },
                        onVerticalDrag = { _, deltaY ->
                            val totalRows =
                                (lazyGridState.layoutInfo.totalItemsCount + appDrawerSettings.appDrawerColumns - 1) / appDrawerSettings.appDrawerColumns

                            val visibleRows =
                                ceil(lazyGridState.layoutInfo.viewportSize.height / appDrawerRowsHeightPx.toFloat()).toInt()

                            val scrollableRows = (totalRows - visibleRows).coerceAtLeast(0)

                            val availableScroll = scrollableRows * appDrawerRowsHeightPx

                            val thumbHeightPx = with(density) { thumbHeight.toPx() }

                            val availableHeight =
                                lazyGridState.layoutInfo.viewportSize.height - thumbHeightPx - bottomPadding

                            thumbY = (thumbY + deltaY).coerceIn(0f, availableHeight)

                            val progress = thumbY / availableHeight

                            val targetScrollY = progress * availableScroll

                            val targetRow = targetScrollY / appDrawerRowsHeightPx

                            val targetIndex =
                                (targetRow * appDrawerSettings.appDrawerColumns).roundToInt()
                                    .coerceIn(0, eblanApplicationInfos.lastIndex)

                            scope.launch {
                                onScrollToItem(targetIndex)
                            }
                        },
                        onDragEnd = {
                            isDraggingThumb = false
                        },
                        onDragCancel = {
                            isDraggingThumb = false
                        },
                    )
                }
                .alpha(thumbAlpha)
                .size(width = 8.dp, height = thumbHeight)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(10.dp),
                ),
        )
    }
}
