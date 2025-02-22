package com.eblan.launcher.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GridCell(val row: Int, val column: Int)
