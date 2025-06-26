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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmapOrNull
import coil.compose.AsyncImage
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.feature.home.component.DragGridSubcomposeLayout
import com.eblan.launcher.feature.home.model.Screen

@Composable
fun EditPageScreen(
    modifier: Modifier = Modifier,
    rows: Int,
    columns: Int,
    gridItems: Map<Int, List<GridItem>>,
    onUpdateScreen: (Screen) -> Unit,
) {
    val appWidgetManager = LocalAppWidgetManager.current

    val context = LocalContext.current

    Column(modifier = modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
        ) {
            items(gridItems.keys.toList()) { index ->
                OutlinedCard(
                    modifier = Modifier.padding(5.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.25f)),
                    border = BorderStroke(width = 2.dp, color = Color.White),
                ) {
                    DragGridSubcomposeLayout(
                        modifier = Modifier.height(200.dp),
                        index = index,
                        rows = rows,
                        columns = columns,
                        gridItems = gridItems,
                        gridItemContent = { gridItem ->
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
                                        AsyncImage(
                                            model = appWidgetInfo.loadPreviewImage(context, 0)
                                                .toBitmapOrNull(),
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