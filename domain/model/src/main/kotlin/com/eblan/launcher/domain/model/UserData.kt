package com.eblan.launcher.domain.model

data class UserData(
    val rows: Int,
    val columns: Int,
    val pageCount: Int,
    val infiniteScroll: Boolean,
)