package com.example.namo.data.entity.diary

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "allEvent")
data class DiaryList(
    @PrimaryKey(autoGenerate = false)
    val eventId:Int,
    val event_title:String,
    val event_category_color:Int,
    val event_start:Long,
    val diary_content:String,
    val diary_img:List<Bitmap>,
    val event_place:String
)