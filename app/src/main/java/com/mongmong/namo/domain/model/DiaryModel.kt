package com.mongmong.namo.domain.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.mongmong.namo.presentation.config.BaseResponse
import com.google.gson.annotations.SerializedName
import com.mongmong.namo.BR
import com.mongmong.namo.presentation.state.RoomState
import java.util.Calendar

data class PersonalDiary(
    val diaryId: Long = 0L,  // roomDB scheduleId
    var scheduleServerId: Long = 0L, // server scheduleId
    private var _content: String? = null,
    var images: List<DiaryImage>? = null,
    var state: String = RoomState.DEFAULT.state,
    var isUpload: Boolean = false,
    var isHeader: Boolean = false
) : BaseObservable() {
    @get:Bindable
    var content: String?
        get() = _content
        set(value) {
            _content = value
            notifyPropertyChanged(BR.content)
        }
}

/*data class DiaryImage(
    val id: Long,
    val url: String
) : Serializable*/


/** 기록 전체 조회 **/
data class DiaryGetAllResponse(
    val result: List<DiaryGetAllResult>
) : BaseResponse()

data class DiaryGetAllResult(
    val scheduleId: Long,
    val contents: String?,
    val urls: List<String>,
)

data class GetMoimMemoResponse(
    val result: MoimDiary
): BaseResponse()

/** 기록 월 별 조회 **/
data class DiaryGetMonthResponse(
    val result: DiaryGetMonthResult
) : BaseResponse()

data class DiaryGetMonthResult(
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
    var images: List<DiaryImage>,
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

    fun getImageUrls() = this.images.map { it.imageUrl }
}

data class DiarySchedule(
    var scheduleId: Long = 0L,
    var title: String = "",
    var startDate: Long = 0,
    var categoryId: Long = 0L,
    var place: String = "없음",
    var content: String?,
    var images: List<DiaryImage>?,
    var serverId: Long = 0L, // scheduleServerId
    var categoryServerId: Long = 0L,
    var color: Int = 1,
    var isHeader: Boolean = false
)

/** v2 Model (ui 레이어에서 비즈니스 로직에서 사용)*/
data class Diary(
    val categoryInfo: CategoryInfo,
    val diarySummary: DiarySummary,
    val startDate: String,
    val endDate: String,
    val scheduleId: Long,
    val scheduleType: Int,
    val title: String,
    val isHeader: Boolean = false,
    val participantInfo: ParticipantSummary
)

data class CategoryInfo(
    val name: String,
    val colorId: Int
)

data class ParticipantSummary(
    val count: Int,
    val names: String,
)

data class DiarySummary(
    val content: String = "",
    val diaryId: Long,
    val diaryImages: List<DiaryImage>? = emptyList()
)

data class DiaryImage(
    val diaryImageId: Long,
    val imageUrl: String,
    val orderNumber: Int
)

class DiaryDetail(
    content: String = "",
    val diaryId: Long = 0,
    diaryImages: List<DiaryImage> = emptyList(),
    enjoyRating: Int = 0
) : BaseObservable() {

    @get:Bindable
    var content: String = content
        set(value) {
            field = value
            notifyPropertyChanged(BR.content)
        }

    @get:Bindable
    var diaryImages: List<DiaryImage> = diaryImages
        set(value) {
            field = value
            notifyPropertyChanged(BR.diaryImages)
        }

    @get:Bindable
    var enjoyRating: Int = enjoyRating
        set(value) {
            field = value
            notifyPropertyChanged(BR.enjoyRating)
        }

    fun copy(
        content: String = this.content,
        diaryImages: List<DiaryImage> = this.diaryImages,
        enjoyRating: Int = this.enjoyRating
    ): DiaryDetail {
        return DiaryDetail(
            content = content,
            diaryId = this.diaryId,
            diaryImages = diaryImages,
            enjoyRating = enjoyRating
        )
    }
}

data class ScheduleForDiary(
    val scheduleId: Long = 0,
    val date: String = "",
    val location: ScheduleForDiaryLocation,
    var hasDiary: Boolean = false,
    val title: String = "",
    val categoryId: Int,
    val participantInfo: List<ParticipantInfo>,
    val participantCount: Int
)

data class ScheduleForDiaryLocation(
    val kakaoLocationId: String = "",
    val name: String = ""
)

data class ParticipantInfo(
    val userId: Long,
    val nickname: String,
    val isGuest: Boolean = true
)

/** 기록 캘린더 */
data class CalendarDay(
    val date: Int,
    val year: Int,
    val month: Int,
    val isEmpty: Boolean = false
) {
    fun isAfterToday(): Boolean {
        if(isEmpty) return false
        val today = Calendar.getInstance()
        val calendarDayDate = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, date)
        }
        return calendarDayDate.after(today)
    }

    fun toDateString(): String {
        val monthString = String.format("%02d", month + 1)
        val dayString = String.format("%02d", date)

        return "${year}-${monthString}-${dayString}"
    }

    fun isSameDate(otherDate: CalendarDay): Boolean {
        return this.year == otherDate.year && this.month == otherDate.month && this.date == otherDate.date
    }

    val displayDate: String
        get() {
            val day = date
            val monthDisplay = month + 1 // 월은 0부터 시작하므로 1을 더해줍니다.
            return if (day == 1) {
                "$monthDisplay/$day"
            } else {
                day.toString()
            }
        }
}

data class CalendarDiaryDate(
    val dates: List<String>,
    val month: Int,
    val year: Int
)
