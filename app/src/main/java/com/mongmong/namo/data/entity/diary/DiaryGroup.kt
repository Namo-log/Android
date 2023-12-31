package com.mongmong.namo.data.entity.diary


import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class DiaryGroupEvent(
    @PrimaryKey(autoGenerate = false)
    var place: String = "",
    var pay: Long = 0L,
    var members: List<Long>,
    var imgs: ArrayList<String?>,
    var placeIdx: Long = 0L
) : java.io.Serializable




