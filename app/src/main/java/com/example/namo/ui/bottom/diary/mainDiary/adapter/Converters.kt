package com.example.namo.ui.bottom.diary.mainDiary.adapter

import androidx.room.TypeConverter
import com.google.gson.Gson
import java.io.File

class Converters {
    // json 형태의 문자열로 직렬화하여 저장, 꺼낼 때는 다시 역직렬화하여 원하는 형태의 클래스로 변환
    @TypeConverter
    fun listToJson(value: List<String>?) = Gson().toJson(value)
    @TypeConverter
    fun jsonToList(value: String?) = Gson().fromJson(value, Array<String?>::class.java).toList()

}