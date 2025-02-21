package com.eblan.launcher.data.room.converter

import androidx.room.TypeConverter
import com.eblan.launcher.domain.model.GridCell
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class EblanLauncherTypeConverters {
    @TypeConverter
    fun fromGridCells(value: List<GridCell>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toGridCells(value: String): List<GridCell> {
        val type = object : TypeToken<List<GridCell>>() {}.type

        return Gson().fromJson(value, type)
    }
}