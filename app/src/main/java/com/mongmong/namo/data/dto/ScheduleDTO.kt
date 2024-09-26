package com.mongmong.namo.data.dto

import com.mongmong.namo.presentation.config.BaseResponse
import com.google.gson.annotations.SerializedName
import com.mongmong.namo.domain.model.Schedule

data class ScheduleDefaultResponse (
    var result: String = ""
) : BaseResponse()

// 개인
/** 일정 월별 조회 */
data class GetMonthScheduleResponse (
    @SerializedName("result") val result : List<GetMonthScheduleResult>
) : BaseResponse()

data class GetMonthScheduleResult (
    val scheduleId : Long,
    val title : String,
    val startDate : Long,
    val endDate : Long,
    val alarmDate : List<Int>,
    val locationInfo: ScheduleLocation,
    val categoryInfo : ScheduleCategoryInfo,
    val hasDiary : Boolean?,
    val isMeetingSchedule : Boolean,
) {
    fun convertServerScheduleResponseToLocal(): Schedule {
        return Schedule(
            this.scheduleId, // localId
            this.title,
            this.startDate,
            this.endDate,
            this.locationInfo,
            this.categoryInfo,
            this.alarmDate ?: listOf(),
            this.hasDiary,
            this.isMeetingSchedule
        )
    }
}

data class ScheduleCategoryInfo(
    var categoryId: Long = 0L,
    val colorId: Int = 0,
    val name: String = "",
    val isShare: Boolean = false
)

/** 일정 생성 */
data class PostScheduleResponse (
    val result : Long
) : BaseResponse()

data class ScheduleRequestBody(
    var title: String = "",
    var categoryId: Long = 0L,
    var period: Period,
    var location: ScheduleLocation,
    var reminderTrigger: List<Int>? = listOf(), //TODO: String으로 변경
)

data class Period(
    var startDate: Long = 0L,
    var endDate: Long = 0L,
)

data class ScheduleLocation(
    var longitude: Double = 0.0, // 경도
    var latitude: Double = 0.0, // 위도
    var locationName: String = "없음",
    var kakaoLocationId: String? = ""
)

/** 일정 수정 */
data class EditScheduleResponse (
    @SerializedName("result") val result : EditScheduleResult
) : BaseResponse()

data class EditScheduleResult (
    @SerializedName("scheduleId")  val scheduleId : Long
)

/** 일정 삭제 */
data class DeleteScheduleResponse (
    @SerializedName("result") val result : String
) : BaseResponse()

// 모임
/** 모임 일정 카테고리 수정 */
data class PatchMoimScheduleCategoryRequestBody(
    @SerializedName("moimScheduleId") val moimScheduleId: Long,
    @SerializedName("categoryId") val categoryId : Long
)

/** 모임 일정 알림 리스트 수정 */
data class PatchMoimScheduleAlarmRequestBody(
    @SerializedName("moimScheduleId") val moimScheduleId: Long,
    @SerializedName("alarmDates") val alarmDates : List<Int>
)