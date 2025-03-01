package com.eblan.launcher.feature.home.component

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
import androidx.compose.ui.unit.dp

@Composable
fun ResizableBox(
    modifier: Modifier = Modifier,
    onTopStartDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit,
    onTopEndDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit,
    onBottomStartDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit,
    onBottomEndDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit,
) {
    val commonModifier = Modifier
        .size(30.dp)
        .background(Color.White, shape = CircleShape)

    Box(
        modifier = modifier.border(width = 2.dp, color = Color.White),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset((-15).dp, (-15).dp)
                .then(commonModifier)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = onTopStartDrag,
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
                        onDrag = onTopEndDrag,
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
                        onDrag = onBottomStartDrag,
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
                        onDrag = onBottomEndDrag,
                    )
                },
        )
    }
}