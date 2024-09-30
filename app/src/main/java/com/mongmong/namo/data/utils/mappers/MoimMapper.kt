package com.mongmong.namo.data.utils.mappers

import com.mongmong.namo.data.dto.GetMoimResult
import com.mongmong.namo.domain.model.MoimPreview

object MoimMapper {
    // DTO -> Model
    fun GetMoimResult.toModel(): MoimPreview {
        return MoimPreview(
            moimId = this.meetingScheduleId,
            startDate = this.startDate,
            coverImg = this.imageUrl,
            title = this.title,
            participantCount = this.participantCount,
            participantNicknames = this.participantNicknames
        )
    }
}