package com.eblan.launcher.data.room.converter

import androidx.room.TypeConverter
import com.eblan.launcher.domain.model.GridItemData
import kotlinx.serialization.json.Json

class EblanTypeConverters {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    @TypeConverter
    fun fromGridItemData(value: GridItemData): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toGridItemData(value: String): GridItemData {
        return json.decodeFromString(value)
    }
}