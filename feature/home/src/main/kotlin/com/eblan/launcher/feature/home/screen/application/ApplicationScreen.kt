package com.eblan.launcher.feature.home.screen.application

import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemLayoutInfo
import com.eblan.launcher.domain.model.UserData
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.util.calculatePage
import kotlin.math.roundToInt

@Composable
fun ApplicationScreen(
    modifier: Modifier = Modifier,
    applicationState: ApplicationState,
    currentPage: Int,
    userData: UserData,
    screenSize: IntSize,
    drag: Drag,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    onLongPressedApplicationInfo: (GridItemLayoutInfo) -> Unit,
    onDragStart: () -> Unit,
) {
    val page = calculatePage(
        index = currentPage,
        infiniteScroll = userData.infiniteScroll,
        pageCount = userData.pageCount,
    )

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.Start) {
            onDragStart()
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier.fillMaxWidth(),
    ) {
        items(eblanApplicationInfos) { eblanApplicationInfo ->
            var offset = Offset.Zero

            Column(
                modifier = Modifier
                    .pointerInput(key1 = eblanApplicationInfo) {
                        awaitPointerEventScope {
                            while (true) {
                                val down = awaitFirstDown(requireUnconsumed = false)

                                val longPressChange =
                                    awaitLongPressOrCancellation(down.id)

                                if (longPressChange != null) {
                                    val data = GridItemData.ApplicationInfo(
                                        packageName = eblanApplicationInfo.packageName,
                                        icon = eblanApplicationInfo.icon,
                                        label = eblanApplicationInfo.label,
                                    )

                                    onLongPressedApplicationInfo(
                                        applicationState.getGridItemLayoutInfo(
                                            page = page,
                                            rows = userData.rows,
                                            columns = userData.columns,
                                            x = offset.x.roundToInt(),
                                            y = offset.y.roundToInt(),
                                            screenSize,
                                            data = data
                                        )
                                    )
                                }
                            }
                        }
                    }
                    .onGloballyPositioned { layoutCoordinates ->
                        offset = layoutCoordinates.positionOnScreen()
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AsyncImage(
                    model = eblanApplicationInfo.icon,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                )

                Text(
                    text = eblanApplicationInfo.label,
                )
            }
        }
    }
}