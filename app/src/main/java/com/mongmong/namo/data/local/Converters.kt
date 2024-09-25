package com.mongmong.namo.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.mongmong.namo.domain.model.Schedule

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

class ScheduleListConverters {
    @TypeConverter
    fun listToJson(value : List<Schedule>?) = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value : String?) = Gson().fromJson(value, Array<Schedule?>::class.java).toList()}