package com.eblan.launcher.data.room.converter

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.eblan.launcher.domain.model.GridCell
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject

@ProvidedTypeConverter
class EblanLauncherTypeConverters @Inject constructor(private val gson: Gson){
    @TypeConverter
    fun fromGridCells(value: List<GridCell>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toGridCells(value: String): List<GridCell> {
        val type = object : TypeToken<List<GridCell>>() {}.type

        return gson.fromJson(value, type)
    }
}