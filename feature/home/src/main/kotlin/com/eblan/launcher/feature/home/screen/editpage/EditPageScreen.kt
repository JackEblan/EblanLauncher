package com.eblan.launcher.feature.home.screen.editpage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmapOrNull
import coil.compose.AsyncImage
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.feature.home.component.DraggableItem
import com.eblan.launcher.feature.home.component.GridSubcomposeLayout
import com.eblan.launcher.feature.home.component.dragContainer
import com.eblan.launcher.feature.home.component.rememberGridDragAndDropState
import com.eblan.launcher.feature.home.model.Screen

@Composable
fun EditPageScreen(
    modifier: Modifier = Modifier,
    rows: Int,
    columns: Int,
    pageCount: Int,
    gridItems: Map<Int, List<GridItem>>,
    onUpdateScreen: (Screen) -> Unit,
    onMovePage: (from: Int, to: Int) -> Unit,
) {
    val appWidgetManager = LocalAppWidgetManager.current

    val context = LocalContext.current

    val gridState = rememberLazyGridState()

    val dragDropState = rememberGridDragAndDropState(gridState = gridState, onMove = onMovePage)

    Column(modifier = modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .dragContainer(dragDropState = dragDropState)
                .weight(1f),
            state = gridState,
        ) {
            items(count = pageCount, key = { index -> index }) { index ->
                DraggableItem(dragDropState = dragDropState, index = index) { isDragging ->
                    OutlinedCard(
                        modifier = Modifier.padding(5.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.25f)),
                        border = BorderStroke(width = 2.dp, color = Color.White),
                    ) {
                        GridSubcomposeLayout(
                            modifier = Modifier.height(200.dp),
                            rows = rows,
                            columns = columns,
                            gridItems = gridItems[index],
                            gridItemContent = { gridItem, _, _, _, _ ->
                                when (val gridItemData = gridItem.data) {
                                    is GridItemData.ApplicationInfo -> {
                                        AsyncImage(
                                            model = gridItemData.icon,
                                            contentDescription = null,
                                            modifier = Modifier.padding(2.dp),
                                        )
                                    }

                                    is GridItemData.Widget -> {
                                        val appWidgetInfo =
                                            appWidgetManager.getAppWidgetInfo(appWidgetId = gridItemData.appWidgetId)

                                        if (appWidgetInfo != null) {
                                            val preview = remember {
                                                appWidgetInfo.loadPreviewImage(context, 0)
                                                    .toBitmapOrNull()
                                            }

                                            AsyncImage(
                                                model = preview,
                                                contentDescription = null,
                                            )
                                        }
                                    }
                                }
                            },
                        )
                    }
                }

            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = {
                    onUpdateScreen(Screen.Pager)
                },
            ) {
                Text(text = "Cancel")
            }

            Button(onClick = {}) {
                Text(text = "Save")
            }
        }
    }
}