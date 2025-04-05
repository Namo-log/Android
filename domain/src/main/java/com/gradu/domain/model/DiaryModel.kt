package com.gradu.domain.model

import android.annotation.SuppressLint
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.mongmong.namo.BR
import java.math.BigDecimal
import java.util.Calendar

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
    val startDate: String = "",
    val endDate: String = "",
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
    val participantId: Long,
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

    @SuppressLint("DefaultLocale")
    fun toDateString(): String {
        val monthString = String.format("%02d", month + 1)
        val dayString = String.format("%02d", date)

        return "${year}-${monthString}-${dayString}"
    }

    fun isSameDate(otherDate: CalendarDay?): Boolean {
        return when {
            otherDate == null -> false
            this.date != otherDate.date -> false
            this.month != otherDate.month -> false
            this.year != otherDate.year -> false
            else -> true
        }
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
    val month: Int,
    val year: Int,
    val dates: List<CalendarDate> = emptyList()  // 날짜와 타입을 포함한 리스트
)

data class CalendarDate(
    val date: String,
    val type: ScheduleType
)

data class MoimPayment(
    val moimPaymentParticipants: List<MoimPaymentParticipant>,
    val totalAmount: BigDecimal
)

data class MoimPaymentParticipant(
    val amount: BigDecimal,
    val nickname: String
)