package com.eblan.launcher.feature.home.screen.editpage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.PageItem
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.ApplicationInfoGridItem
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.grid.ShortcutInfoGridItem
import com.eblan.launcher.feature.home.component.grid.WidgetGridItem
import com.eblan.launcher.feature.home.component.grid.gridItem

@Composable
fun EditPageScreen(
    modifier: Modifier = Modifier,
    rows: Int,
    columns: Int,
    rootHeight: Int,
    pageItems: List<PageItem>,
    dockHeight: Int,
    initialPage: Int,
    textColor: TextColor,
    onSaveEditPage: (
        initialPage: Int,
        pageItems: List<PageItem>,
        pageItemsToDelete: List<PageItem>,
    ) -> Unit,
    onCancelEditPage: () -> Unit,
) {
    val density = LocalDensity.current

    val dockHeightDp = with(density) {
        dockHeight.toDp()
    }

    var currentPageItems by remember { mutableStateOf(pageItems) }

    val pageItemsToDelete = remember { mutableStateListOf<PageItem>() }

    var currentInitialPage by remember { mutableIntStateOf(initialPage) }

    val gridState = rememberLazyGridState()

    val gridDragAndDropState =
        rememberGridDragAndDropState(gridState = gridState) { from, to ->
            currentPageItems = currentPageItems.toMutableList().apply { add(to, removeAt(from)) }
        }

    val gridHeight = with(density) {
        ((rootHeight - dockHeight) / 2).toDp()
    }

    val color = when (textColor) {
        TextColor.White -> Color.White
        TextColor.Black -> Color.Black
    }

    Column(modifier = modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .dragContainer(state = gridDragAndDropState)
                .weight(1f),
            state = gridState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            itemsIndexed(
                currentPageItems,
                key = { _, pageItem -> pageItem.id },
            ) { index, pageItem ->
                DraggableItem(state = gridDragAndDropState, index = index) {
                    OutlinedCard(
                        modifier = Modifier.padding(vertical = 10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.25f)),
                        border = BorderStroke(width = 2.dp, color = Color.White),
                    ) {
                        GridLayout(
                            modifier = Modifier.height(gridHeight),
                            rows = rows,
                            columns = columns,
                        ) {
                            pageItem.gridItems.forEach { gridItem ->
                                val gridItemModifier = Modifier.gridItem(gridItem)

                                when (val data = gridItem.data) {
                                    is GridItemData.ApplicationInfo -> {
                                        ApplicationInfoGridItem(
                                            modifier = gridItemModifier,
                                            data = data,
                                            color = color,
                                        )
                                    }

                                    is GridItemData.Widget -> {
                                        WidgetGridItem(
                                            modifier = gridItemModifier,
                                            data = data,
                                        )
                                    }

                                    is GridItemData.ShortcutInfo -> {
                                        ShortcutInfoGridItem(
                                            modifier = gridItemModifier,
                                            data = data,
                                            color = color,
                                        )
                                    }
                                }
                            }
                        }

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
                                    onClick = {
                                        currentPageItems = currentPageItems.toMutableList()
                                            .apply {
                                                removeIf { currentPageItem ->
                                                    currentPageItem.id == pageItem.id
                                                }
                                            }

                                        pageItemsToDelete.add(pageItem)
                                    },
                                    enabled = pageItem.id != currentInitialPage,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        currentInitialPage = pageItem.id
                                    },
                                    enabled = pageItem.id != currentInitialPage,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = null,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(dockHeightDp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Button(
                onClick = {
                    currentPageItems = currentPageItems.toMutableList()
                        .apply { add(PageItem(id = size, gridItems = emptyList())) }
                },
            ) {
                Text(text = "Add")
            }

            Button(
                onClick = {
                    onCancelEditPage()
                },
            ) {
                Text(text = "Cancel")
            }

            Button(
                onClick = {
                    onSaveEditPage(
                        currentInitialPage,
                        currentPageItems,
                        pageItemsToDelete,
                    )
                },
            ) {
                Text(text = "Save")
            }
        }
    }
}