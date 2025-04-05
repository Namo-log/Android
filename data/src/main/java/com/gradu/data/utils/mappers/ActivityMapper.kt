package com.gradu.data.utils.mappers

import com.mongmong.namo.data.dto.ActivityLocationDTO
import com.mongmong.namo.data.dto.PatchActivityPaymentRequest
import com.mongmong.namo.data.dto.GetActivitiesResult
import com.mongmong.namo.data.dto.GetActivityPaymentResult
import com.mongmong.namo.data.dto.Payment
import com.mongmong.namo.domain.model.Activity
import com.mongmong.namo.domain.model.ActivityLocation
import com.mongmong.namo.domain.model.ActivityParticipant
import com.mongmong.namo.domain.model.DiaryImage
import com.mongmong.namo.domain.model.ParticipantInfo
import com.mongmong.namo.domain.model.ActivityPayment
import com.mongmong.namo.domain.model.PaymentParticipant

object ActivityMapper {
    fun GetActivitiesResult.toModel(): Activity {
        return Activity(
            startDate = this.activityStartDate,
            endDate = this.activityEndDate,
            activityId = this.activityId,
            location = ActivityLocation(
                kakaoLocationId = this.activityLocation.kakaoLocationId,
                locationName = this.activityLocation.locationName,
                latitude = this.activityLocation.latitude,
                longitude = this.activityLocation.longitude),
            participants = this.activityParticipants.map {
                ActivityParticipant(
                    nickname = it.participantNickname,
                    participantId = it.participantId,
                    activityParticipantId = it.activityParticipantId
                )
            },
            title = this.activityTitle,
            tag = this.tag,
            payment = ActivityPayment(participants = emptyList()),
            images = this.activityImages.map {
                DiaryImage(diaryImageId = it.activityImageId, imageUrl = it.imageUrl, orderNumber = it.orderNumber)
            }
        )
    }

    fun GetActivityPaymentResult.toModel(): ActivityPayment {
        return ActivityPayment(
            totalAmount = this.totalAmount,
            divisionCount = this.divisionCount,
            amountPerPerson = this.amountPerPerson,
            participants = this.participants.map {
                PaymentParticipant(
                    id = it.activityParticipantId,
                    nickname = it.participantNickname,
                    isPayer = it.includedInSettlement
                ) }
        )
    }

    fun ActivityLocation.toDTO(): ActivityLocationDTO {
        return ActivityLocationDTO(
            kakaoLocationId = this.kakaoLocationId,
            locationName = this.locationName,
            longitude = this.longitude,
            latitude = this.latitude
        )
    }

    fun ActivityPayment.toDTO(): Payment {
        return Payment(
            amountPerPerson = this.amountPerPerson,
            divisionCount = this.divisionCount,
            participantIdList = this.participants.map { it.id },
            totalAmount = this.totalAmount
        )
    }

}