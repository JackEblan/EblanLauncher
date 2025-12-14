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
package com.eblan.launcher.feature.home.component.popup

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import coil3.compose.AsyncImage
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
internal fun ShortcutInfoMenu(
    modifier: Modifier = Modifier,
    currentPage: Int,
    drag: Drag,
    icon: String?,
    eblanShortcutInfosByPackageName: List<EblanShortcutInfo>,
    gridItemSettings: GridItemSettings,
    onResetOverlay: () -> Unit,
    onTapShortcutInfo: (
        serialNumber: Long,
        packageName: String,
        shortcutId: String,
    ) -> Unit,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onDraggingGridItem: () -> Unit,
) {
    Column(
        modifier = modifier
            .sizeIn(
                maxWidth = 300.dp,
                maxHeight = 300.dp,
            )
            .verticalScroll(rememberScrollState()),
    ) {
        eblanShortcutInfosByPackageName.forEach { eblanShortcutInfo ->
            ShortcutInfoMenuItem(
                currentPage = currentPage,
                icon = icon,
                drag = drag,
                gridItemSettings = gridItemSettings,
                onResetOverlay = onResetOverlay,
                onTapShortcutInfo = onTapShortcutInfo,
                eblanShortcutInfo = eblanShortcutInfo,
                onLongPressGridItem = onLongPressGridItem,
                onUpdateGridItemOffset = onUpdateGridItemOffset,
                onDraggingGridItem = onDraggingGridItem,
            )
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
private fun ShortcutInfoMenuItem(
    modifier: Modifier = Modifier,
    currentPage: Int,
    drag: Drag,
    icon: String?,
    gridItemSettings: GridItemSettings,
    onResetOverlay: () -> Unit,
    onTapShortcutInfo: (Long, String, String) -> Unit,
    eblanShortcutInfo: EblanShortcutInfo,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onDraggingGridItem: () -> Unit,
) {
    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val scale = remember { Animatable(1f) }

    var alpha by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.End || drag == Drag.Cancel) {
            alpha = 1f

            scale.stop()

            if (scale.value < 1f) {
                scale.animateTo(1f)
            }

            onResetOverlay()
        }
    }

    ListItem(
        modifier = modifier
            .clickable {
                onTapShortcutInfo(
                    eblanShortcutInfo.serialNumber,
                    eblanShortcutInfo.packageName,
                    eblanShortcutInfo.shortcutId,
                )
            },
        headlineContent = {
            Text(text = eblanShortcutInfo.shortLabel)
        },
        leadingContent = {
            AsyncImage(
                model = eblanShortcutInfo.icon,
                contentDescription = null,
                modifier = Modifier
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }

                        drawLayer(graphicsLayer)
                    }
                    .pointerInput(key1 = drag) {
                        detectTapGestures(
                            onLongPress = {
                                scope.launch {
                                    scale.animateTo(0.5f)

                                    scale.animateTo(1f)

                                    val data = GridItemData.ShortcutInfo(
                                        shortcutId = eblanShortcutInfo.shortcutId,
                                        packageName = eblanShortcutInfo.packageName,
                                        serialNumber = eblanShortcutInfo.serialNumber,
                                        shortLabel = eblanShortcutInfo.shortLabel,
                                        longLabel = eblanShortcutInfo.longLabel,
                                        icon = eblanShortcutInfo.icon,
                                        isEnabled = eblanShortcutInfo.isEnabled,
                                        eblanApplicationInfoIcon = icon,
                                        customIcon = null,
                                        customShortLabel = null,
                                    )

                                    onLongPressGridItem(
                                        GridItemSource.New(
                                            gridItem = GridItem(
                                                id = Uuid.random()
                                                    .toHexString(),
                                                folderId = null,
                                                page = currentPage,
                                                startColumn = -1,
                                                startRow = -1,
                                                columnSpan = 1,
                                                rowSpan = 1,
                                                data = data,
                                                associate = Associate.Grid,
                                                override = false,
                                                gridItemSettings = gridItemSettings,
                                            ),
                                        ),
                                        graphicsLayer.toImageBitmap(),
                                    )

                                    onUpdateGridItemOffset(
                                        intOffset,
                                        intSize,
                                    )

                                    onDraggingGridItem()

                                    alpha = 0f
                                }
                            },
                            onPress = {
                                awaitRelease()

                                scale.stop()

                                alpha = 1f

                                onResetOverlay()

                                if (scale.value < 1f) {
                                    scale.animateTo(1f)
                                }
                            },
                        )
                    }
                    .alpha(alpha)
                    .scale(
                        scaleX = scale.value,
                        scaleY = scale.value,
                    )
                    .onGloballyPositioned { layoutCoordinates ->
                        intOffset =
                            layoutCoordinates.positionInRoot().round()

                        intSize = layoutCoordinates.size
                    }
                    .size(30.dp),
            )
        },
    )
}
