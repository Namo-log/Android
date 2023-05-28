package com.example.namo.data.entity.diary

import androidx.room.PrimaryKey

/** dummy **/
data class DiaryGroupEvent (
    @PrimaryKey(autoGenerate = true)
    val place:String="",
     val pay:Int=0,
    // val members:List<Int>?,
    val imgs:List<String>?
    // val imgs:List<List<String>>
)
data class GroupDiaryMember(
    val memberName:String=""
)

data class CheckPeople(
    var memberName : String = "",
    var isChecked : Boolean = false
)