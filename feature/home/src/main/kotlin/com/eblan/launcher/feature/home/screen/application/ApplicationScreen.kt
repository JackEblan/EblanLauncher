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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GridItemData
import kotlin.math.roundToInt

@Composable
fun ApplicationScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    pageCount: Int,
    screenSize: IntSize,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    onLongPressApplicationInfo: (Offset, IntSize) -> Unit,
    onAddApplicationInfoGridItem: (
        page: Int,
        x: Int,
        y: Int,
        rowSpan: Int,
        columnSpan: Int,
        screenWidth: Int,
        screenHeight: Int,
        data: GridItemData,
    ) -> Unit,
) {
    val z = currentPage - (Int.MAX_VALUE / 2)

    val page = z - z.floorDiv(pageCount) * pageCount

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier.fillMaxWidth(),
    ) {
        items(eblanApplicationInfos) { eblanApplicationInfo ->
            var eblanApplicationInfoSize = IntSize.Zero

            var eblanApplicationInfoOffset = Offset.Zero

            Column(
                modifier = Modifier
                    .pointerInput(key1 = eblanApplicationInfo) {
                        awaitPointerEventScope {
                            while (true) {
                                val down = awaitFirstDown(requireUnconsumed = false)

                                val longPressChange =
                                    awaitLongPressOrCancellation(down.id) ?: continue

                                if (!longPressChange.isConsumed) {
                                    val data = GridItemData.ApplicationInfo(
                                        packageName = eblanApplicationInfo.packageName,
                                        icon = eblanApplicationInfo.icon,
                                        label = eblanApplicationInfo.label,
                                    )

                                    onLongPressApplicationInfo(
                                        eblanApplicationInfoOffset,
                                        eblanApplicationInfoSize,
                                    )

                                    onAddApplicationInfoGridItem(
                                        page,
                                        eblanApplicationInfoOffset.x.roundToInt(),
                                        eblanApplicationInfoOffset.y.roundToInt(),
                                        1,
                                        1,
                                        screenSize.width,
                                        screenSize.height,
                                        data,
                                    )
                                }
                            }
                        }
                    }
                    .onSizeChanged { intSize ->
                        eblanApplicationInfoSize = intSize
                    }
                    .onGloballyPositioned { layoutCoordinates ->
                        eblanApplicationInfoOffset = layoutCoordinates.positionOnScreen()
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