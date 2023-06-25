package com.example.namo.ui.bottom.diary.adapter

import android.media.metrics.Event
import androidx.room.TypeConverter
import com.google.gson.Gson
import java.io.File

class StringListConverters {
    @TypeConverter
    fun listToJson(value: List<String>?) = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value: String?) = Gson().fromJson(value, Array<String?>::class.java).toList()

}

class IntListConverters {
    @TypeConverter
    fun listToJson(value: List<Int>?) = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value: String?) = Gson().fromJson(value, Array<Int?>::class.java).toList()
}

class EventListConverters {
    @TypeConverter
    fun listToJson(value : List<Event>?) = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value : String?) = Gson().fromJson(value, Array<Event?>::class.java).toList()
}