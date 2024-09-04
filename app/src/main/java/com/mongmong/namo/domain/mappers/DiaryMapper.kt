package com.mongmong.namo.domain.mappers

import com.mongmong.namo.data.dto.GetDiaryCollectionResult
import com.mongmong.namo.domain.model.Diary
import com.mongmong.namo.domain.model.DiaryImage
import com.mongmong.namo.domain.model.DiarySummary

fun GetDiaryCollectionResult.toDiary(): Diary {
    return Diary(
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
        participantsCount = this.participantsCount,
        participantsNames = this.participantsNames,
        scheduleDate = this.scheduleDate,
        scheduleId = this.scheduleId,
        scheduleType = this.scheduleType,
        title = this.title
    )
}
