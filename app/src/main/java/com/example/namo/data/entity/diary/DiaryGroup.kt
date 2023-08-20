package com.example.namo.data.entity.diary


import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class DiaryGroupEvent(
    @PrimaryKey(autoGenerate = false)
    var place: String = "",
    var pay: Int = 0,
    var members: List<Int>,
    var imgs: ArrayList<String?>
) :java.io.Serializable


