package com.mongmong.namo.data.utils.mappers

import com.mongmong.namo.data.dto.GetCalendarDiaryResult
import com.mongmong.namo.data.dto.GetDiaryByDateResult
import com.mongmong.namo.data.dto.GetDiaryCollectionResult
import com.mongmong.namo.data.dto.GetPersonalDiaryResult
import com.mongmong.namo.data.dto.GetScheduleForDiaryResult
import com.mongmong.namo.domain.model.CalendarDiaryDate
import com.mongmong.namo.domain.model.CategoryInfo
import com.mongmong.namo.domain.model.Diary
import com.mongmong.namo.domain.model.DiaryDetail
import com.mongmong.namo.domain.model.DiaryImage
import com.mongmong.namo.domain.model.DiarySummary
import com.mongmong.namo.domain.model.ParticipantInfo
import com.mongmong.namo.domain.model.ScheduleForDiary

object DiaryMapper {
    // 매퍼 함수 (DTO -> 도메인 모델 변환)
    fun GetDiaryCollectionResult.toModel(): Diary {
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
            participantInfo = ParticipantInfo(
                count = this.participantInfo.participantsCount,
                names = this.participantInfo.participantsNames ?: ""
            )
        )
    }

    fun GetScheduleForDiaryResult.toModel(): ScheduleForDiary {
        return ScheduleForDiary(
            scheduleId = this.scheduleId,
            title = this.scheduleTitle,
            date = this.scheduleStartDate,
            location = this.locationInfo.locationName,
            categoryId = this.categoryInfo.colorId,
            hasDiary = this.hasDiary
        )
    }


    fun GetPersonalDiaryResult.toModel(): DiaryDetail {
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
        return CalendarDiaryDate(
            dates = this.dates,
            year = this.year,
            month = this.month
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
            participantInfo = ParticipantInfo(
                count = this.participantInfo.participantsCount,
                names = this.participantInfo.participantsNames ?: ""
            )
        )
    }
}

