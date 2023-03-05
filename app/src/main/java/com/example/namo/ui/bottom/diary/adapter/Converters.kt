package com.example.namo.ui.bottom.diary.adapter

import androidx.room.TypeConverter
import com.google.gson.Gson
import java.io.File

class Converters {
    @TypeConverter
    fun listToJson(value: List<File>?) = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value: String?) = Gson().fromJson(value, Array<File?>::class.java).toList()

}