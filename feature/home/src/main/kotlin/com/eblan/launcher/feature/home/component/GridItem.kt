package com.eblan.launcher.feature.home.component

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.unit.Density

data class GridItemParentData(
    val width: Int,
    val height: Int,
    val x: Int,
    val y: Int,
)

fun Modifier.gridItem(
    width: Int,
    height: Int,
    x: Int,
    y: Int,
): Modifier = then(
    object : ParentDataModifier {
        override fun Density.modifyParentData(parentData: Any?): Any {
            return GridItemParentData(
                width = width,
                height = height,
                x = x,
                y = y,
            )
        }
    },
)

@Composable
fun GridItemContainer(
    modifier: Modifier = Modifier,
    rowSpan: Int,
    columnSpan: Int,
    startRow: Int,
    startColumn: Int,
    cellWidth: Int,
    cellHeight: Int,
    content: @Composable BoxScope.() -> Unit,
) {
    val width = columnSpan * cellWidth

    val height = rowSpan * cellHeight

    val x = startColumn * cellWidth

    val y = startRow * cellHeight

    Box(
        modifier = modifier.gridItem(
            width = width,
            height = height,
            x = x,
            y = y,
        ),
        content = content,
    )
}

@Composable
fun AnimatedGridItemContainer(
    modifier: Modifier = Modifier,
    rowSpan: Int,
    columnSpan: Int,
    startRow: Int,
    startColumn: Int,
    cellWidth: Int,
    cellHeight: Int,
    content: @Composable BoxScope.() -> Unit,
) {
    val width by animateIntAsState(targetValue = columnSpan * cellWidth)

    val height by animateIntAsState(targetValue = rowSpan * cellHeight)

    val x by animateIntAsState(targetValue = startColumn * cellWidth)

    val y by animateIntAsState(targetValue = startRow * cellHeight)

    Box(
        modifier = modifier.gridItem(
            width = width,
            height = height,
            x = x,
            y = y,
        ),
        content = content,
    )
}