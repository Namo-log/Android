package com.example.namo.data.entity.diary


import androidx.room.PrimaryKey

/** dummy **/
data class DiaryGroupEvent(
    @PrimaryKey(autoGenerate = true)
    var place: String? = "",
    var pay: Int = 0,
    var imgs: ArrayList<String?>
) :java.io.Serializable


data class GroupDiaryMember(
    val memberName: String = ""
)
