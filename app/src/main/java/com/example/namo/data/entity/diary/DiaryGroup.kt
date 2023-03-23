package com.example.namo.data.entity.diary

import androidx.room.PrimaryKey

/** dummy **/
data class DiaryGroupEvent (
    @PrimaryKey(autoGenerate = true)
    val eventId:Int,
    val place:String="",
    val pay:Int=0,
    val members:List<Int>?,
    val imgs:List<String>?,
    val isChecked: Boolean=false

)
data class GroupDiaryMember(
    val memberName:String=""
)


