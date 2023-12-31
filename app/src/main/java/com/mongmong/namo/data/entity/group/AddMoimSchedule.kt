package com.mongmong.namo.data.entity.group

import com.google.gson.annotations.SerializedName

data class AddMoimSchedule(
    @SerializedName("moimId") var moimId : Long = 0L,
    @SerializedName("name") var name : String = "",
    @SerializedName("startDate") var startLong : Long = 0L,
    @SerializedName("endDate") var endLong : Long = 0L,
    @SerializedName("interval") var interval : Int = 0,
    @SerializedName("x") var x : Double = 0.0,
    @SerializedName("y") var y : Double = 0.0,
    @SerializedName("locationName") var locationName : String = "",
    @SerializedName("users") var users : List<Long> = listOf()
)

data class EditMoimSchedule(
    @SerializedName("moimScheduleId") var moimScheduleId : Long = 0L,
    @SerializedName("name") var name : String = "",
    @SerializedName("startDate") var startLong : Long = 0L,
    @SerializedName("endDate") var endLong : Long = 0L,
    @SerializedName("interval") var interval : Int = 0,
    @SerializedName("x") var x : Double = 0.0,
    @SerializedName("y") var y : Double = 0.0,
    @SerializedName("locationName") var locationName : String = "",
    @SerializedName("users") var users : List<Long> = listOf()
)
