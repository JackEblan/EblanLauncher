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
package com.eblan.launcher.feature.home.screen.editpage

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.PageItem
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.util.getGridItemTextColor
import com.eblan.launcher.feature.home.util.getSystemTextColor
import java.io.File

@Composable
internal fun EditPageScreen(
    modifier: Modifier = Modifier,
    screenHeight: Int,
    pageItems: List<PageItem>,
    textColor: TextColor,
    paddingValues: PaddingValues,
    homeSettings: HomeSettings,
    iconPackInfoPackageName: String,
    onSaveEditPage: (
        id: Int,
        pageItems: List<PageItem>,
        pageItemsToDelete: List<PageItem>,
    ) -> Unit,
    onUpdateScreen: (Screen) -> Unit,
) {
    val density = LocalDensity.current

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val bottomPadding = with(density) {
        paddingValues.calculateBottomPadding().roundToPx()
    }

    val verticalPadding = topPadding + bottomPadding

    val gridHeight = screenHeight - verticalPadding

    var currentPageItems by remember { mutableStateOf(pageItems) }

    val pageItemsToDelete = remember { mutableStateListOf<PageItem>() }

    var selectedId by remember { mutableIntStateOf(homeSettings.initialPage) }

    val lazyGridState = rememberLazyGridState()

    val gridDragAndDropState =
        rememberLazyGridDragAndDropState(gridState = lazyGridState) { from, to ->
            currentPageItems = currentPageItems.toMutableList().apply { add(to, removeAt(from)) }
        }

    val cardHeight = with(density) {
        ((gridHeight - homeSettings.dockHeight) / 2).toDp()
    }

    val isAtTop by remember(key1 = lazyGridState) {
        derivedStateOf {
            lazyGridState.firstVisibleItemIndex == 0 && lazyGridState.firstVisibleItemScrollOffset == 0
        }
    }

    BackHandler {
        onUpdateScreen(Screen.Pager)
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .dragContainer(state = gridDragAndDropState)
                .matchParentSize(),
            state = lazyGridState,
            contentPadding = paddingValues,
        ) {
            itemsIndexed(
                items = currentPageItems,
                key = { _, pageItem -> pageItem.id },
            ) { index, pageItem ->
                DraggableItem(
                    modifier = Modifier.padding(5.dp),
                    state = gridDragAndDropState,
                    index = index,
                ) {
                    Column(
                        modifier = Modifier
                            .height(cardHeight)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(8.dp),
                            ),
                    ) {
                        GridLayout(
                            modifier = Modifier.weight(1f),
                            gridItems = pageItem.gridItems,
                            columns = homeSettings.columns,
                            rows = homeSettings.rows,
                            { gridItem ->
                                GridItemContent(
                                    modifier = Modifier.padding(2.dp),
                                    gridItem = gridItem,
                                    textColor = textColor,
                                    iconPackInfoPackageName = iconPackInfoPackageName,
                                )
                            },
                        )

                        PageButtons(
                            pageItem = pageItem,
                            selectedId = selectedId,
                            onDeleteClick = {
                                currentPageItems = currentPageItems.toMutableList().apply {
                                    removeIf { currentPageItem ->
                                        currentPageItem.id == pageItem.id
                                    }
                                }

                                pageItemsToDelete.add(pageItem)
                            },
                            onHomeClick = {
                                selectedId = pageItem.id
                            },
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(paddingValues),
            visible = isAtTop,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            ActionButtons(
                onCancel = {
                    onUpdateScreen(Screen.Pager)
                },
                onAdd = {
                    currentPageItems = currentPageItems.toMutableList().apply {
                        add(PageItem(id = size, gridItems = emptyList()))
                    }
                },
                onSave = {
                    onSaveEditPage(
                        selectedId,
                        currentPageItems,
                        pageItemsToDelete,
                    )
                },
            )
        }
    }
}

@Composable
private fun PageButtons(
    pageItem: PageItem,
    selectedId: Int,
    onDeleteClick: () -> Unit,
    onHomeClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        shape = RoundedCornerShape(30.dp),
        tonalElevation = 10.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            IconButton(
                onClick = onDeleteClick,
                enabled = pageItem.id != selectedId,
            ) {
                Icon(
                    imageVector = EblanLauncherIcons.Delete,
                    contentDescription = null,
                )
            }

            IconButton(
                onClick = onHomeClick,
                enabled = pageItem.id != selectedId,
            ) {
                Icon(
                    imageVector = EblanLauncherIcons.Home,
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
private fun ActionButtons(
    modifier: Modifier = Modifier,
    onCancel: () -> Unit,
    onAdd: () -> Unit,
    onSave: () -> Unit,
) {
    Surface(
        modifier = modifier.padding(10.dp),
        shape = RoundedCornerShape(30.dp),
        tonalElevation = 10.dp,
    ) {
        Row(
            modifier = Modifier.padding(5.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = EblanLauncherIcons.Close,
                    contentDescription = null,
                )
            }

            IconButton(onClick = onSave) {
                Icon(
                    imageVector = EblanLauncherIcons.Save,
                    contentDescription = null,
                )
            }

            IconButton(onClick = onAdd) {
                Icon(
                    imageVector = EblanLauncherIcons.Add,
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
private fun GridItemContent(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    textColor: TextColor,
    iconPackInfoPackageName: String,
) {
    val context = LocalContext.current

    val currentTextColor = if (gridItem.override) {
        getGridItemTextColor(
            systemTextColor = textColor,
            gridItemTextColor = gridItem.gridItemSettings.textColor,
        )
    } else {
        getSystemTextColor(textColor = textColor)
    }

    key(gridItem.id) {
        when (val data = gridItem.data) {
            is GridItemData.ApplicationInfo -> {
                val iconPacksDirectory = File(context.filesDir, FileManager.ICON_PACKS_DIR)

                val iconPackDirectory = File(iconPacksDirectory, iconPackInfoPackageName)

                val iconFile = File(iconPackDirectory, data.packageName)

                val icon = if (iconPackInfoPackageName.isNotEmpty() && iconFile.exists()) {
                    iconFile.absolutePath
                } else {
                    data.icon
                }

                AsyncImage(
                    model = icon,
                    contentDescription = null,
                    modifier = modifier,
                )
            }

            is GridItemData.Widget -> {
                AsyncImage(
                    model = data.preview,
                    contentDescription = null,
                    modifier = modifier,
                )
            }

            is GridItemData.ShortcutInfo -> {
                AsyncImage(
                    model = data.icon,
                    contentDescription = null,
                    modifier = modifier,
                )
            }

            is GridItemData.Folder -> {
                if (data.icon != null) {
                    AsyncImage(
                        model = data.icon,
                        contentDescription = null,
                        modifier = modifier,
                    )
                } else {
                    Icon(
                        imageVector = EblanLauncherIcons.Folder,
                        contentDescription = null,
                        modifier = modifier,
                        tint = currentTextColor,
                    )
                }
            }

            is GridItemData.ShortcutConfig -> {
                AsyncImage(
                    model = data.shortcutIntentIcon ?: data.activityIcon,
                    contentDescription = null,
                    modifier = modifier,
                )
            }
        }
    }
}
