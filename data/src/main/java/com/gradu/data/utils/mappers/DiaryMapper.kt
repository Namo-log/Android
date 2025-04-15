package com.gradu.data.utils.mappers

import com.gradu.data.dto.GetCalendarDiaryResult
import com.gradu.data.dto.GetDiaryArchiveResult
import com.gradu.data.dto.GetDiaryByDateResult
import com.gradu.data.dto.GetDiaryResult
import com.gradu.data.dto.GetMoimPaymentResult
import com.gradu.data.dto.GetScheduleForDiaryResult
import com.gradu.domain.model.CalendarDate
import com.gradu.domain.model.CalendarDiaryDate
import com.gradu.domain.model.CategoryInfo
import com.gradu.domain.model.Diary
import com.gradu.domain.model.DiaryDetail
import com.gradu.domain.model.DiaryImage
import com.gradu.domain.model.DiarySummary
import com.gradu.domain.model.MoimPayment
import com.gradu.domain.model.MoimPaymentParticipant
import com.gradu.domain.model.ParticipantInfo
import com.gradu.domain.model.ParticipantSummary
import com.gradu.domain.model.ScheduleForDiary
import com.gradu.domain.model.ScheduleForDiaryLocation
import com.gradu.domain.model.ScheduleType

object DiaryMapper {
    // 매퍼 함수 (DTO -> 도메인 모델 변환)
    fun GetDiaryArchiveResult.toModel(): Diary {
        return Diary(
            categoryInfo = CategoryInfo(
                name = this.categoryInfo.name,
                colorId = this.categoryInfo.colorId
            ),
            diarySummary = DiarySummary(
                content = this.diarySummary.content,
                diaryId = this.diarySummary.diaryId,
                diaryImages = this.diarySummary.diaryImages?.map { image ->
                    DiaryImage(
                        diaryImageId = image.diaryImageId,
                        imageUrl = image.imageUrl,
                        orderNumber = image.orderNumber
                    )
                }
            ),
            startDate = this.scheduleStartDate,
            endDate = this.scheduleEndDate,
            scheduleId = this.scheduleId,
            scheduleType = this.scheduleType,
            title = this.title,
            isHeader = this.isHeader,
            participantInfo = ParticipantSummary(
                count = this.participantInfo.participantsCount,
                names = this.participantInfo.participantsNames ?: ""
            )
        )
    }

    fun GetScheduleForDiaryResult.toModel(): ScheduleForDiary {
        return ScheduleForDiary(
            scheduleId = this.scheduleId,
            title = this.scheduleTitle,
            startDate = this.scheduleStartDate,
            endDate = this.scheduleEndDate,
            location = ScheduleForDiaryLocation(
                kakaoLocationId = this.locationInfo.kakaoLocationId,
                name = this.locationInfo.locationName
            ),
            categoryId = this.categoryInfo.colorId,
            hasDiary = this.hasDiary,
            participantInfo = this.participantInfo.map {
                ParticipantInfo(
                    userId = it.userId,
                    participantId = it.participantId,
                    nickname = it.nickname,
                    isGuest = it.isGuest
                )
            },
            participantCount = this.participantCount
        )
    }


    fun GetDiaryResult.toModel(): DiaryDetail {
        return DiaryDetail(
            content = this.content,
            diaryId = this.diaryId, // Int에서 Long으로 변환
            diaryImages = this.diaryImages.map { image ->
                DiaryImage(
                    diaryImageId = image.diaryImageId,
                    imageUrl = image.imageUrl,
                    orderNumber = image.orderNumber
                )
            },
            enjoyRating = this.enjoyRating
        )
    }

    fun GetCalendarDiaryResult.toModel(): CalendarDiaryDate {
        val personalDates = this.diaryDateForPersonal.map { CalendarDate(date = it, type = ScheduleType.PERSONAL) }
        val meetingDates = this.diaryDateForMeeting.map { CalendarDate(date = it, type = ScheduleType.MOIM) }
        val birthDates = this.diaryDateForBirthday.map { CalendarDate(date = it, type = ScheduleType.BIRTHDAY) }

        return CalendarDiaryDate(
            year = this.year,
            month = this.month,
            dates = personalDates + meetingDates + birthDates // 세 리스트를 합쳐서 dates에 추가
        )
    }


    fun GetDiaryByDateResult.toModel(): Diary {
        return Diary(
            categoryInfo = CategoryInfo(
                name = this.categoryInfo.name,
                colorId = this.categoryInfo.colorId
            ),
            diarySummary = DiarySummary(diaryId = this.diaryId),
            startDate = this.scheduleStartDate,
            endDate = this.scheduleEndDate,
            scheduleId = this.scheduleId,
            scheduleType = this.scheduleType,
            title = this.scheduleTitle,
            participantInfo = ParticipantSummary(
                count = this.participantInfo.participantsCount,
                names = this.participantInfo.participantsNames ?: ""
            )
        )
    }

    fun GetMoimPaymentResult.toModel(): MoimPayment {
        return MoimPayment(
            totalAmount = this.totalAmount,
            moimPaymentParticipants = this.settlementUserList.map {
                MoimPaymentParticipant(
                    amount = it.amount,
                    nickname = it.nickname
                )
            }
        )
    }
}

