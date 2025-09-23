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

import android.content.Intent
import android.graphics.Rect
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.window.Popup
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.addLastModifiedToFileCacheKey
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.model.AppDrawerSettings
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.HorizontalAlignment
import com.eblan.launcher.domain.model.VerticalArrangement
import com.eblan.launcher.feature.home.component.menu.ApplicationInfoMenu
import com.eblan.launcher.feature.home.component.menu.MenuPositionProvider
import com.eblan.launcher.feature.home.component.overscroll.OffsetOverscrollEffect
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.EblanApplicationComponentUiState
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.screen.loading.LoadingScreen
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.feature.home.util.getSystemTextColor
import com.eblan.launcher.ui.local.LocalLauncherApps
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.roundToInt
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun DoubleTapApplicationScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    eblanApplicationComponentUiState: EblanApplicationComponentUiState,
    paddingValues: PaddingValues,
    drag: Drag,
    screenHeight: Int,
    appDrawerSettings: AppDrawerSettings,
    eblanApplicationInfosByLabel: List<EblanApplicationInfo>,
    gridItemSource: GridItemSource?,
    iconPackInfoPackageName: String,
    onLongPressGridItem: (
        currentPage: Int,
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdateGridItemOffset: (IntOffset) -> Unit,
    onGetEblanApplicationInfosByLabel: (String) -> Unit,
    onDismiss: () -> Unit,
    onDraggingGridItem: () -> Unit,
) {
    val animatedSwipeUpY = remember { Animatable(screenHeight.toFloat()) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = animatedSwipeUpY) {
        animatedSwipeUpY.animateTo(0f)
    }

    ApplicationScreen(
        modifier = modifier.offset {
            IntOffset(x = 0, y = animatedSwipeUpY.value.roundToInt())
        },
        currentPage = currentPage,
        pageCount = pageCount,
        infiniteScroll = infiniteScroll,
        eblanApplicationComponentUiState = eblanApplicationComponentUiState,
        paddingValues = paddingValues,
        drag = drag,
        appDrawerSettings = appDrawerSettings,
        eblanApplicationInfosByLabel = eblanApplicationInfosByLabel,
        gridItemSource = gridItemSource,
        iconPackInfoPackageName = iconPackInfoPackageName,
        onLongPressGridItem = onLongPressGridItem,
        onUpdateGridItemOffset = onUpdateGridItemOffset,
        onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
        onDismiss = onDismiss,
        onAnimateDismiss = {
            scope.launch {
                animatedSwipeUpY.animateTo(screenHeight.toFloat())

                onDismiss()
            }
        },
        onDraggingGridItem = onDraggingGridItem,
    )
}

@Composable
fun ApplicationScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    eblanApplicationComponentUiState: EblanApplicationComponentUiState,
    paddingValues: PaddingValues,
    drag: Drag,
    appDrawerSettings: AppDrawerSettings,
    eblanApplicationInfosByLabel: List<EblanApplicationInfo>,
    gridItemSource: GridItemSource?,
    iconPackInfoPackageName: String,
    onLongPressGridItem: (
        currentPage: Int,
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdateGridItemOffset: (IntOffset) -> Unit,
    onGetEblanApplicationInfosByLabel: (String) -> Unit,
    onDismiss: () -> Unit,
    onAnimateDismiss: () -> Unit,
    onDraggingGridItem: () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    var showPopupApplicationMenu by remember { mutableStateOf(false) }

    val page = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    var popupMenuIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var popupMenuIntSize by remember { mutableStateOf(IntSize.Zero) }

    val scope = rememberCoroutineScope()

    val overscrollAlpha = remember { Animatable(0f) }

    val overscrollOffset = remember { Animatable(0f) }

    val overscrollEffect = remember(key1 = scope) {
        OffsetOverscrollEffect(
            scope = scope,
            overscrollAlpha = overscrollAlpha,
            overscrollOffset = overscrollOffset,
            onFling = onDismiss,
            onFastFling = onAnimateDismiss,
        )
    }

    BackHandler {
        showPopupApplicationMenu = false

        onAnimateDismiss()
    }

    Surface(
        modifier = modifier
            .graphicsLayer(alpha = 1f - (overscrollAlpha.value / 500f))
            .fillMaxSize(),
    ) {
        when (eblanApplicationComponentUiState) {
            EblanApplicationComponentUiState.Loading -> {
                LoadingScreen()
            }

            is EblanApplicationComponentUiState.Success -> {
                val eblanApplicationInfos =
                    eblanApplicationComponentUiState.eblanApplicationComponent.eblanApplicationInfos

                Box(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    when {
                        eblanApplicationInfos.isEmpty() -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }

                        else -> {
                            Column(
                                modifier = Modifier
                                    .offset {
                                        IntOffset(x = 0, y = overscrollOffset.value.roundToInt())
                                    }
                                    .matchParentSize()
                                    .padding(
                                        top = paddingValues.calculateTopPadding(),
                                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                                    ),
                            ) {
                                EblanApplicationInfoDockSearchBar(
                                    page = page,
                                    onQueryChange = onGetEblanApplicationInfosByLabel,
                                    eblanApplicationInfosByLabel = eblanApplicationInfosByLabel,
                                    drag = drag,
                                    appDrawerSettings = appDrawerSettings,
                                    iconPackInfoPackageName = iconPackInfoPackageName,
                                    onLongPress = { intOffset, intSize ->
                                        onUpdateGridItemOffset(intOffset)

                                        popupMenuIntOffset = intOffset

                                        popupMenuIntSize = intSize

                                        focusManager.clearFocus()
                                    },
                                    onLongPressGridItem = onLongPressGridItem,
                                    onDraggingGridItem = {
                                        onDraggingGridItem()

                                        showPopupApplicationMenu = false
                                    },
                                    onUpdatePopupMenu = {
                                        showPopupApplicationMenu = true
                                    },
                                )

                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(count = appDrawerSettings.appDrawerColumns),
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding()),
                                    overscrollEffect = overscrollEffect,
                                ) {
                                    items(eblanApplicationInfos) { eblanApplicationInfo ->
                                        EblanApplicationInfoItem(
                                            page = page,
                                            drag = drag,
                                            eblanApplicationInfo = eblanApplicationInfo,
                                            appDrawerSettings = appDrawerSettings,
                                            iconPackInfoPackageName = iconPackInfoPackageName,
                                            onLongPress = { intOffset, intSize ->
                                                onUpdateGridItemOffset(intOffset)

                                                popupMenuIntOffset = intOffset

                                                popupMenuIntSize = intSize
                                            },
                                            onLongPressGridItem = onLongPressGridItem,
                                            onDraggingGridItem = {
                                                onDraggingGridItem()

                                                showPopupApplicationMenu = false
                                            },
                                            onUpdatePopupMenu = {
                                                showPopupApplicationMenu = true
                                            },
                                        )
                                    }
                                }
                            }

                            if (showPopupApplicationMenu && gridItemSource?.gridItem != null) {
                                PopupApplicationInfoMenu(
                                    paddingValues = paddingValues,
                                    popupMenuIntOffset = popupMenuIntOffset,
                                    gridItem = gridItemSource.gridItem,
                                    popupMenuIntSize = popupMenuIntSize,
                                    onDismissRequest = {
                                        showPopupApplicationMenu = false
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EblanApplicationInfoDockSearchBar(
    modifier: Modifier = Modifier,
    page: Int,
    drag: Drag,
    appDrawerSettings: AppDrawerSettings,
    onQueryChange: (String) -> Unit,
    eblanApplicationInfosByLabel: List<EblanApplicationInfo>,
    iconPackInfoPackageName: String,
    onLongPress: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onLongPressGridItem: (
        currentPage: Int,
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onDraggingGridItem: () -> Unit,
    onUpdatePopupMenu: () -> Unit,
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
                    page = page,
                    drag = drag,
                    eblanApplicationInfo = eblanApplicationInfo,
                    appDrawerSettings = appDrawerSettings,
                    iconPackInfoPackageName = iconPackInfoPackageName,
                    onLongPress = onLongPress,
                    onLongPressGridItem = onLongPressGridItem,
                    onDraggingGridItem = onDraggingGridItem,
                    onUpdatePopupMenu = onUpdatePopupMenu,
                )
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
private fun EblanApplicationInfoItem(
    modifier: Modifier = Modifier,
    page: Int,
    drag: Drag,
    eblanApplicationInfo: EblanApplicationInfo,
    appDrawerSettings: AppDrawerSettings,
    iconPackInfoPackageName: String,
    onLongPress: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onLongPressGridItem: (
        currentPage: Int,
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdatePopupMenu: () -> Unit,
    onDraggingGridItem: () -> Unit,
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

    val appDrawerRowsHeightDp = with(density) {
        appDrawerSettings.appDrawerRowsHeight.toDp()
    }

    val iconSizeDp = with(density) {
        appDrawerSettings.gridItemSettings.iconSize.toDp()
    }

    val textSizeSp = with(density) {
        appDrawerSettings.gridItemSettings.textSize.toSp()
    }

    val maxLines = if (appDrawerSettings.gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    var isLongPressed by remember { mutableStateOf(false) }

    val iconPacksDirectory = File(context.filesDir, FileManager.ICON_PACKS_DIR)

    val iconPackDirectory = File(iconPacksDirectory, iconPackInfoPackageName)

    val iconFile = File(iconPackDirectory, eblanApplicationInfo.packageName)

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

    LaunchedEffect(key1 = drag) {
        if (isLongPressed) {
            when (drag) {
                Drag.Dragging -> {
                    onDraggingGridItem()
                }

                Drag.Cancel, Drag.End -> {
                    isLongPressed = false
                }

                else -> Unit
            }
        }
    }

    Column(
        modifier = modifier
            .drawWithContent {
                graphicsLayer.record {
                    drawContext.transform.scale(
                        scaleX = scale.value,
                        scaleY = scale.value,
                    )

                    this@drawWithContent.drawContent()
                }

                drawLayer(graphicsLayer)
            }
            .pointerInput(key1 = drag) {
                detectTapGestures(
                    onTap = {
                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            launcherApps.startMainActivity(
                                componentName = eblanApplicationInfo.componentName,
                                sourceBounds = Rect(
                                    intOffset.x,
                                    intOffset.y,
                                    intOffset.x + intSize.width,
                                    intOffset.y + intSize.height
                                )
                            )
                        }
                    },
                    onLongPress = {
                        isLongPressed = true

                        onLongPress(
                            intOffset,
                            intSize,
                        )

                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            val data =
                                GridItemData.ApplicationInfo(
                                    componentName = eblanApplicationInfo.componentName,
                                    packageName = eblanApplicationInfo.packageName,
                                    icon = eblanApplicationInfo.icon,
                                    label = eblanApplicationInfo.label,
                                )

                            onLongPressGridItem(
                                page,
                                GridItemSource.New(
                                    gridItem = GridItem(
                                        id = Uuid.random()
                                            .toHexString(),
                                        folderId = null,
                                        page = page,
                                        startColumn = 0,
                                        startRow = 0,
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

                            onUpdatePopupMenu()
                        }
                    },
                    onPress = {
                        awaitRelease()

                        isLongPressed = false
                    },
                )
            }
            .onGloballyPositioned { layoutCoordinates ->
                intOffset =
                    layoutCoordinates.positionInRoot().round()

                intSize = layoutCoordinates.size
            }
            .height(appDrawerRowsHeightDp),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        Spacer(modifier = Modifier.height(5.dp))

        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(icon)
                .addLastModifiedToFileCacheKey(true)
                .build(),
            contentDescription = null,
            modifier = Modifier.size(iconSizeDp),
        )

        if (appDrawerSettings.gridItemSettings.showLabel) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = eblanApplicationInfo.label.toString(),
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = maxLines,
                fontSize = textSizeSp,
            )
        }
    }
}

@Composable
private fun PopupApplicationInfoMenu(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    popupMenuIntOffset: IntOffset,
    gridItem: GridItem?,
    popupMenuIntSize: IntSize,
    onDismissRequest: () -> Unit,
) {
    val applicationInfo = gridItem?.data as? GridItemData.ApplicationInfo ?: return

    val density = LocalDensity.current

    val launcherApps = LocalLauncherApps.current

    val context = LocalContext.current

    val leftPadding = with(density) {
        paddingValues.calculateStartPadding(LayoutDirection.Ltr).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val x = popupMenuIntOffset.x - leftPadding

    val y = popupMenuIntOffset.y - topPadding

    Popup(
        popupPositionProvider = MenuPositionProvider(
            x = x,
            y = y,
            width = popupMenuIntSize.width,
            height = popupMenuIntSize.height,
        ),
        onDismissRequest = onDismissRequest,
        content = {
            ApplicationInfoMenu(
                modifier = modifier,
                onApplicationInfo = {
                    launcherApps.startAppDetailsActivity(
                        componentName = applicationInfo.componentName,
                        sourceBounds = Rect(
                            x,
                            y,
                            x + popupMenuIntSize.width,
                            y + popupMenuIntSize.height
                        )
                    )

                    onDismissRequest()
                },
                onDelete = {
                    val intent = Intent(Intent.ACTION_DELETE).apply {
                        data = "package:${applicationInfo.packageName}".toUri()
                    }

                    context.startActivity(intent)

                    onDismissRequest()
                },
            )
        },
    )
}
