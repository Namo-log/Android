package com.example.namo.data.entity.diary


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="diaryTable")
data class Diary(
    @PrimaryKey(autoGenerate = true)
    var diaryIdx:Int=0,

    var scheduleIdx:Int,
    var content:String
)