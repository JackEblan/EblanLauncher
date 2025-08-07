package com.eblan.launcher.domain.model

data class MoveGridItemResult(
    val isSuccess: Boolean,
    val movingGridItem: GridItem,
    val conflictingGridItem: GridItem?,
)
