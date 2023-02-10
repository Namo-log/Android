package com.example.namo.data.entity.diary

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="diaryGalleryTable")
data class Gallery(
    @PrimaryKey(autoGenerate = true)
    var imgIdx:Int=0,

    var diaryIdx:Int,
    var url:String
)