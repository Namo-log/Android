package com.example.namo.data.entity.diary

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diaryTable")
data class Diary(
    @PrimaryKey(autoGenerate = false)
    val scheduleIdx: Int,
    var content: String = "",
    var images: List<String>? = null
)