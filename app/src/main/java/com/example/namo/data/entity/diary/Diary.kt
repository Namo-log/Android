package com.example.namo.data.entity.diary

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.File

@Entity(tableName="diaryTable")
data class Diary(
    @PrimaryKey(autoGenerate = true)
    var scheduleIdx:Int,

    @ColumnInfo(name = "diary_content")
    var content:String="",

    @ColumnInfo(name = "diary_img")
    var imgs:List<File>?=null
)

