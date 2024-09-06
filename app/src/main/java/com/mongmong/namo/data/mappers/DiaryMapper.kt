package com.mongmong.namo.data.mappers

import com.mongmong.namo.data.dto.GetDiaryCollectionResult
import com.mongmong.namo.data.dto.GetPersonalDiaryResult
import com.mongmong.namo.domain.model.CategoryInfo
import com.mongmong.namo.domain.model.Diary
import com.mongmong.namo.domain.model.DiaryDetail
import com.mongmong.namo.domain.model.DiaryImage
import com.mongmong.namo.domain.model.DiarySummary
import com.mongmong.namo.domain.model.ParticipantInfo

object DiaryMapper {
    // 매퍼 함수 (DTO -> 도메인 모델 변환)
    fun GetDiaryCollectionResult.toDiary(): Diary {
        return Diary(
            categoryInfo = CategoryInfo(
                name = this.categoryInfo.name,
                color = this.categoryInfo.color
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


    fun GetPersonalDiaryResult.toDiaryDetail(): DiaryDetail {
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
}

