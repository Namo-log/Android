package com.example.namo.data

import androidx.room.TypeConverter
import com.google.gson.Gson

class StringListConverters {
    @TypeConverter
    fun listToJson(value: List<String>?): String = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value: String?) = Gson().fromJson(value, Array<String?>::class.java).toList()

}

class IntListConverters {
    @TypeConverter
    fun listToJson(value: List<Int>?) = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value: String?) = Gson().fromJson(value, Array<Int?>::class.java).toList()
}