package com.eblan.launcher.data.room.converter

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.eblan.launcher.domain.model.GridItemData
import kotlinx.serialization.json.Json
import javax.inject.Inject

@ProvidedTypeConverter
class EblanTypeConverters @Inject constructor() {
    @TypeConverter
    fun fromGridItemData(value: GridItemData): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toGridItemData(value: String): GridItemData {
        return Json.decodeFromString(value)
    }
}