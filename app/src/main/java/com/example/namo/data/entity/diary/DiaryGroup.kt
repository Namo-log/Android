package com.example.namo.data.entity.diary


import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class DiaryGroupEvent(
    @PrimaryKey(autoGenerate = false)
    @SerializedName("name") var place: String? = "",
    @SerializedName("money") var pay: Int = 0,
    @SerializedName("participants") var members: MutableList<String>,
    @SerializedName("imgs") var imgs: ArrayList<String?>
) :java.io.Serializable


