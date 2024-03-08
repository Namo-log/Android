package com.mongmong.namo.data.local.entity.diary


import androidx.room.PrimaryKey

data class DiaryGroupEvent(
    @PrimaryKey(autoGenerate = false)
    var place: String = "",
    var pay: Long = 0L,
    var members: List<Long>,
    var imgs: ArrayList<String?>,
    var placeIdx: Long = 0L
) : java.io.Serializable




