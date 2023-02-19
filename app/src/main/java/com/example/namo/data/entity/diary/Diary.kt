package com.example.namo.data.entity.diary


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="diaryTable")
data class Diary(
    @PrimaryKey(autoGenerate = true)
    var diaryIdx:Int=0,

    var title:String="",
    var date:Long=0,
    var categoryColor:Int=0,
    var content:String="",
    var imgList:List<String>,
    var yearMonth:String="", // "2023.02"
    var place:String=""
)