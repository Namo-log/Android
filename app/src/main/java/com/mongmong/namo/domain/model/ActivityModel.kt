package com.mongmong.namo.domain.model

import com.mongmong.namo.presentation.config.BaseResponse

data class Activity(
    var endDate: String,
    val activityId: Long,
    var location: ActivityLocation,
    var participants: List<ParticipantInfo>,
    var startDate: String,
    var title: String,
    var tag: String,
    var payment: ActivityPayment,
    var images: List<DiaryImage>
)


data class ActivityLocation(
    val kakaoLocationId: String = "",
    val latitude: Double = 0.0,
    val locationName: String = "",
    val longitude: Double = 0.0
)

data class ActivityPayment(
    var totalAmount: Int = 0,
    var divisionCount: Int = 0,
    var amountPerPerson: Int = 0,
    var participants: List<PaymentParticipant>
)

data class PaymentParticipant(
    val id: Long = 0,
    val nickname: String = "",
    var isPayer: Boolean = false
)

data class DiaryBaseResponse(
    val result: String = ""
): BaseResponse()