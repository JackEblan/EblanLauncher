package com.eblan.launcher.domain.model

sealed interface GridItemMovement {
    data object Left : GridItemMovement

    data object Right : GridItemMovement

    data class Inside(val gridItem: GridItem) : GridItemMovement
}