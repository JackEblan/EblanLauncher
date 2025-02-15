package com.eblan.launcher.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.eblan.launcher.feature.home.geometry.ResizableBoundingBox

@Composable
fun ResizableBox(
    modifier: Modifier = Modifier,
    resizableBoundingBox: ResizableBoundingBox,
    onTopStartDragEnd: () -> Unit,
    onTopStartDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit,
    onTopEndDragEnd: () -> Unit,
    onTopEndDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit,
    onBottomStartDragEnd: () -> Unit,
    onBottomStartDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit,
    onBottomEndDragEnd: () -> Unit,
    onBottomEndDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit,
) {
    val density = LocalDensity.current

    val commonModifier = Modifier
        .size(30.dp)
        .background(Color.White, shape = CircleShape)

    val resizableBoundingBoxWidth = with(density) {
        resizableBoundingBox.width.toDp()
    }

    val resizableBoundingBoxHeight = with(density) {
        resizableBoundingBox.height.toDp()
    }

    Box(modifier = modifier
        .offset {
            IntOffset(x = resizableBoundingBox.x, y = resizableBoundingBox.y)
        }
        .size(
            width = resizableBoundingBoxWidth, height = resizableBoundingBoxHeight
        )
        .border(width = 2.dp, color = Color.White)) {
        Box(modifier = Modifier
            .align(Alignment.TopStart)
            .offset((-15).dp, (-15).dp)
            .then(commonModifier)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = onTopStartDragEnd, onDrag = onTopStartDrag
                )
            })

        Box(modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(15.dp, (-15).dp)
            .then(commonModifier)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = onTopEndDragEnd, onDrag = onTopEndDrag
                )
            })

        Box(modifier = Modifier
            .align(Alignment.BottomStart)
            .offset((-15).dp, 15.dp)
            .then(commonModifier)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = onBottomStartDragEnd, onDrag = onBottomStartDrag
                )
            })

        Box(modifier = Modifier
            .align(Alignment.BottomEnd)
            .offset(15.dp, 15.dp)
            .then(commonModifier)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = onBottomEndDragEnd, onDrag = onBottomEndDrag
                )
            })
    }
}