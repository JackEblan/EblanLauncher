package com.eblan.launcher.feature.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.geometry.calculateMenuCoordinates
import com.eblan.launcher.domain.geometry.calculateResizableBoundingBox
import com.eblan.launcher.domain.model.BoundingBox
import com.eblan.launcher.domain.model.Coordinates
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.ResizableBoundingBox
import kotlin.math.roundToInt

@Composable
fun ResizableBoxWithMenu(
    modifier: Modifier = Modifier,
    gridItem: GridItem?,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    screenWidth: Int,
    screenHeight: Int,
    onDragEnd: () -> Unit,
    onTopStartDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit,
    onTopEndDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit,
    onBottomStartDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit,
    onBottomEndDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit,
    onEdit: (Int) -> Unit,
) {
    val density = LocalDensity.current

    val resizableBoundingBox = calculateResizableBoundingBox(
        coordinates = Coordinates(
            x = x, y = y,
        ),
        boundingBox = BoundingBox(
            width = width, height = height,
        ),
    )

    val menuSizeMarginPixel = with(density) {
        20.dp.toPx()
    }.roundToInt()

    val commonModifier = Modifier
        .size(30.dp)
        .background(Color.White, shape = CircleShape)

    val resizableBoundingBoxWidth = with(density) {
        resizableBoundingBox.width.toDp()
    }

    val resizableBoundingBoxHeight = with(density) {
        resizableBoundingBox.height.toDp()
    }

    Box(
        modifier = modifier
            .offset {
                IntOffset(x = resizableBoundingBox.x, y = resizableBoundingBox.y)
            }
            .size(
                width = resizableBoundingBoxWidth, height = resizableBoundingBoxHeight,
            )
            .border(width = 2.dp, color = Color.White),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset((-15).dp, (-15).dp)
                .then(commonModifier)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = onDragEnd, onDrag = onTopStartDrag,
                    )
                },
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(15.dp, (-15).dp)
                .then(commonModifier)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = onDragEnd, onDrag = onTopEndDrag,
                    )
                },
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset((-15).dp, 15.dp)
                .then(commonModifier)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = onDragEnd, onDrag = onBottomStartDrag,
                    )
                },
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(15.dp, 15.dp)
                .then(commonModifier)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = onDragEnd, onDrag = onBottomEndDrag,
                    )
                },
        )
    }

    Menu(
        gridItem = gridItem,
        resizableBoundingBox = resizableBoundingBox,
        screenWidth = screenWidth,
        screenHeight = screenHeight,
        menuSizeMarginPixel = menuSizeMarginPixel,
        onEdit = onEdit,
    )
}

@Composable
private fun Menu(
    modifier: Modifier = Modifier,
    gridItem: GridItem?,
    resizableBoundingBox: ResizableBoundingBox,
    screenWidth: Int,
    screenHeight: Int,
    menuSizeMarginPixel: Int,
    onEdit: (Int) -> Unit,
) {
    Surface(
        modifier = modifier.layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)

            layout(
                width = placeable.width,
                height = placeable.height,
            ) {
                val menuCoordinates = calculateMenuCoordinates(
                    parentX = resizableBoundingBox.x,
                    parentY = resizableBoundingBox.y,
                    parentWidth = resizableBoundingBox.width,
                    parentHeight = resizableBoundingBox.height,
                    childWidth = placeable.width,
                    childHeight = placeable.height,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    margin = menuSizeMarginPixel,
                )

                placeable.placeRelative(x = menuCoordinates.x, y = menuCoordinates.y)
            }
        },
        shape = RoundedCornerShape(30.dp),
        shadowElevation = 2.dp,
    ) {
        Row {
            IconButton(
                onClick = {
                    gridItem?.id?.let(onEdit)
                },
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null)
            }

            Spacer(modifier = Modifier.width(5.dp))

            IconButton(
                onClick = {

                },
            ) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = null)
            }
        }
    }
}