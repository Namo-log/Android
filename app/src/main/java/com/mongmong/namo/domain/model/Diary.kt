package com.mongmong.namo.domain.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.mongmong.namo.presentation.config.BaseResponse
import com.google.gson.annotations.SerializedName
import com.mongmong.namo.BR

data class GetPersonalDiaryResponse(
    val result: GetPersonalDiaryResult
): BaseResponse()

data class GetPersonalDiaryResult(
    val contents: String,
    val urls: List<String>
)


data class DiaryResponse(
    val result: String
) : BaseResponse() // 기본 string


/** 기록 추가 **/
data class DiaryAddResponse(
    val result: DiaryAddResult
) : BaseResponse()

data class DiaryAddResult(
    val scheduleId: Long
)

/** 기록 월 별 조회 **/
data class DiaryGetAllResponse(
    val result: List<DiaryGetAllResult>
) : BaseResponse()

data class DiaryGetAllResult(
    val scheduleId: Long,
    val contents: String?,
    val urls: List<String>,
)

data class DiarySchedule(
    var scheduleId: Long = 0L,
    var title: String = "",
    var startDate: Long = 0,
    var categoryId: Long = 0L,
    var place: String = "없음",
    var content: String?,
    var images: List<String>? = null,
    var serverId: Long = 0L, // scheduleServerId
    var categoryServerId : Long = 0L,
    var color: Int = 1,
    var isHeader: Boolean = false
)

data class GetMoimMemoResponse(
    val result: MoimDiary
): BaseResponse()

/** 모임 기록 월 별 조회 **/
data class DiaryGetMonthResponse(
    val result: GroupResult
) : BaseResponse()

data class GroupResult(
    val content: List<MoimDiary>,
    val currentPage: Int,
    val size: Int,
    val first: Boolean,
    val last: Boolean
)

data class MoimDiary(
    var scheduleId: Long,
    @SerializedName("name") var title: String,
    var startDate: Long,
    @SerializedName("contents") var _content: String?,
    var urls: List<String>,
    var categoryId: Long,
    var color: Int,
    var placeName: String
) : java.io.Serializable, BaseObservable() {
    @get:Bindable
    var content: String?
        get() = _content
        set(value) {
            _content = value
            notifyPropertyChanged(BR.content)
        }
}


