package com.example.data
import androidx.room.TypeConverter
class StringListConverter {
    @TypeConverter
    fun fromString(value: String): List<String> {
        if (value.isBlank()) return emptyList()
        return value.split(",")
    }
    @TypeConverter
    fun toString(list: List<String>): String {
        return list.joinToString(",")
    }
}
