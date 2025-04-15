package com.gradu.data.dto

import com.gradu.domain.model.BaseResponse
import java.math.BigDecimal

data class GetActivitiesResponse(
    val result: List<GetActivitiesResult>
): BaseResponse()

data class GetActivitiesResult(
    val activityEndDate: String,
    val activityId: Long,
    val activityLocation: ActivityLocationDTO,
    val activityParticipants: List<ActivityParticipant>,
    val activityStartDate: String,
    val activityTitle: String,
    val tag: String,
    val totalAmount: Int,
    val activityImages: List<ActivityImage>
)

data class ActivityParticipant(
    val participantId: Long,
    val activityParticipantId: Long,
    val participantNickname: String
)

data class ActivityLocationDTO(
    val kakaoLocationId: String,
    val latitude: Double,
    val locationName: String,
    val longitude: Double
)

data class ActivityImage(
    val orderNumber: Int,
    val imageUrl: String,
    val activityImageId: Long
)

data class GetActivityPaymentResponse(
    val result: GetActivityPaymentResult
): BaseResponse()

data class GetActivityPaymentResult(
    val amountPerPerson: BigDecimal = BigDecimal.ZERO,
    val divisionCount: Int = 0,
    val participants: List<PaymentParticipant> = emptyList(),
    val totalAmount: BigDecimal = BigDecimal.ZERO
)

data class PaymentParticipant(
    val activityParticipantId: Long = 0L,
    val includedInSettlement: Boolean = false,
    val participantNickname: String = ""
)

data class PostActivityRequest(
    val activityEndDate: String,
    val activityStartDate: String,
    val imageList: List<String>,
    val location: ActivityLocationDTO,
    val participantIdList: List<Long>,
    val settlement: Payment,
    val tag: String,
    val title: String
)

data class Payment(
    val amountPerPerson: BigDecimal,
    val divisionCount: Int,
    val participantIdList: List<Long>,
    val totalAmount: BigDecimal
)

data class PatchActivityPaymentRequest(
    val amountPerPerson: BigDecimal,
    val divisionCount: Int,
    val activityParticipantId: List<Long>,
    val totalAmount: BigDecimal
)

data class PatchActivityParticipantsRequest(
    val participantsToAdd: List<Long>,
    val participantsToRemove: List<Long>
)

data class PatchActivityRequest(
    val activityEndDate: String,
    val activityStartDate: String,
    val deleteImages: List<Long>,
    val imageList: List<String>,
    val location: ActivityLocationDTO,
    val title: String
)

