package com.eblan.launcher.feature.home.screen.grid.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.unit.Density

data class GridItemParentData(
    val width: Int,
    val height: Int,
    val x: Int,
    val y: Int,
)

fun Modifier.animateGridItemPlacement(
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
