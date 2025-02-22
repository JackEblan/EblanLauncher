package com.eblan.launcher.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class EblanLauncherApplicationInfoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val gridItemId: Int,
    val packageName: String,
    val flags: Int,
    val label: String,
)