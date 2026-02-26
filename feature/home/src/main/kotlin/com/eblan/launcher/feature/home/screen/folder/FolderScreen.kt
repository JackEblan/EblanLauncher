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

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.addLastModifiedToFileCacheKey
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.FolderGridLayout
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.util.getGridItemTextColor
import com.eblan.launcher.feature.home.util.getHorizontalAlignment
import com.eblan.launcher.feature.home.util.getSystemTextColor
import com.eblan.launcher.feature.home.util.getVerticalArrangement
import com.eblan.launcher.ui.local.LocalSettings

@Composable
internal fun FolderScreen(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    popupIntOffset: IntOffset,
    popupIntSize: IntSize,
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
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onDraggingGridItem: () -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
) {
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

    val x = popupIntOffset.x - leftPadding

    val y = popupIntOffset.y - topPadding

    val data = gridItem.data as? GridItemData.Folder ?: error("Expected GridItemData.Folder")

    val cellWidth = safeDrawingWidth / homeSettings.columns

    val cellHeight = safeDrawingHeight / homeSettings.rows

    val gridPaddingDp = 10.dp

    val gridWidthDp = with(density) {
        (cellWidth * data.columns).toDp()
    }

    val gridHeightDp = with(density) {
        (cellHeight * data.rows).toDp()
    }

    Layout(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        awaitRelease()

                        onDismissRequest()
                    },
                )
            }
            .fillMaxSize()
            .padding(paddingValues),
        content = {
            Surface(
                modifier = Modifier
                    .size(
                        width = gridWidthDp,
                        height = gridHeightDp,
                    )
                    .padding(gridPaddingDp),
                shape = RoundedCornerShape(5.dp),
                shadowElevation = 2.dp,
                content = {
                    HorizontalPager(
                        state = folderGridHorizontalPagerState,
                    ) { index ->
                        FolderGridLayout(
                            modifier = Modifier.fillMaxSize(),
                            gridItems = data.gridItemsByPage[index],
                            columns = data.columns,
                            rows = data.rows,
                            { gridItem ->
                                ApplicationInfoGridItemContent(
                                    gridItem = gridItem,
                                    textColor = textColor,
                                    gridItemSettings = gridItemSettings,
                                    statusBarNotifications = statusBarNotifications,
                                    iconPackFilePaths = iconPackFilePaths,
                                )
                            },
                        )
                    }
                },
            )
        },
    ) { measurables, constraints ->
        val placeable = measurables.first().measure(
            constraints.copy(
                minWidth = 0,
                minHeight = 0,
            ),
        )

        val parentCenterX = x + popupIntSize.width / 2

        val childX = (parentCenterX - placeable.width / 2).coerceIn(
            0,
            constraints.maxWidth - placeable.width,
        )

        val topY = y - placeable.height
        val bottomY = y + popupIntSize.height

        val childY = (if (topY < 0) bottomY else topY).coerceIn(
            0,
            constraints.maxHeight - placeable.height,
        )

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.place(
                x = childX,
                y = childY,
            )
        }
    }
}

@Composable
private fun ApplicationInfoGridItemContent(
    modifier: Modifier = Modifier,
    gridItem: ApplicationInfoGridItem,
    textColor: TextColor,
    gridItemSettings: GridItemSettings,
    statusBarNotifications: Map<String, Int>,
    iconPackFilePaths: Map<String, String>,
) {
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

    val settings = LocalSettings.current

    val maxLines = if (currentGridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    val icon = iconPackFilePaths[gridItem.componentName] ?: gridItem.icon

    val hasNotifications =
        statusBarNotifications[gridItem.packageName] != null && (statusBarNotifications[gridItem.packageName]
            ?: 0) > 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(currentGridItemSettings.padding.dp)
            .background(
                color = Color(currentGridItemSettings.customBackgroundColor),
                shape = RoundedCornerShape(size = currentGridItemSettings.cornerRadius.dp),
            ),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        Box(modifier = Modifier.size(currentGridItemSettings.iconSize.dp)) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(gridItem.customIcon ?: icon)
                    .addLastModifiedToFileCacheKey(true).build(),
                contentDescription = null,
                modifier = modifier.matchParentSize(),
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