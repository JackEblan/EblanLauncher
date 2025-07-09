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
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.PageItem
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.grid.gridItem

@Composable
fun EditPageScreen(
    modifier: Modifier = Modifier,
    rows: Int,
    columns: Int,
    rootHeight: Int,
    pageItems: List<PageItem>,
    dockHeight: Int,
    onSaveEditPage: (pageItems: List<PageItem>) -> Unit,
    onCancelEditPage: () -> Unit,
) {
    val density = LocalDensity.current

    val dockHeightDp = with(density) {
        dockHeight.toDp()
    }

    var currentPageItems by remember { mutableStateOf(pageItems) }

    val gridState = rememberLazyGridState()

    val gridDragAndDropState =
        rememberGridDragAndDropState(gridState = gridState) { from, to ->
            currentPageItems = currentPageItems.toMutableList().apply { add(to, removeAt(from)) }
        }

    val gridHeight = with(density) {
        ((rootHeight - dockHeight) / 2).toDp()
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
                                when (val data = gridItem.data) {
                                    is GridItemData.ApplicationInfo -> {
                                        AsyncImage(
                                            model = data.icon,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .gridItem(gridItem)
                                                .padding(2.dp),
                                        )
                                    }

                                    is GridItemData.Widget -> {
                                        AsyncImage(
                                            model = data.preview,
                                            contentDescription = null,
                                            modifier = Modifier.gridItem(gridItem),
                                        )
                                    }
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
                    onCancelEditPage()
                },
            ) {
                Text(text = "Cancel")
            }

            Button(
                onClick = {
                    onSaveEditPage(currentPageItems)
                },
            ) {
                Text(text = "Save")
            }
        }
    }
}