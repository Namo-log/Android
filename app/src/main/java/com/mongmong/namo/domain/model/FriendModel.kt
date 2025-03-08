package com.mongmong.namo.domain.model

import org.joda.time.LocalDateTime
import java.io.Serializable

data class Friend(
    val userId: Long,
    val profileUrl: String?,
    val nickname: String,
    val tag: String,
    val introduction: String,
    val name: String,
    val birth: String,
    val isFavorite: Boolean, // 즐겨찾기 여부
    val favoriteColorId: Int,
): Serializable {
    fun convertToFriendInfo(): FriendInfo {
        return FriendInfo(
            profileUrl = this.profileUrl,
            nickname = this.nickname,
            tag = this.tag,
            name = this.name,
            introduction = this.introduction,
            birth = this.birth,
            favoriteColorId = this.favoriteColorId
        )
    }
}

data class FriendSchedule(
    val scheduleId: Long = 0L,
    val title: String = "",
    val startDate: LocalDateTime = LocalDateTime.now(),
    val endDate: LocalDateTime = LocalDateTime.now(),
    val categoryInfo: ScheduleCategoryInfo
) {
    fun convertToCommunityModel(): CommunityCommonSchedule {
        return CommunityCommonSchedule(
            scheduleId = this.scheduleId,
            title = this.title,
            startDate = this.startDate,
            endDate = this.endDate,
            participants = null,
            categoryInfo = this.categoryInfo,
            type = ScheduleType.PERSONAL
        )
    }
}

data class FriendRequest(
    var userId: Long = 0L,
    var friendRequestId: Long = 0L,
    var profileUrl: String? = "",
    val nickname: String = "",
    val tag: String = "",
    var introduction: String = "",
    val name: String,
    var birth: String = "",
    var favoriteColorId: Int = 0
) {
    fun convertToFriendInfo(): FriendInfo {
        return FriendInfo(
            profileUrl = this.profileUrl,
            nickname = this.nickname,
            tag = this.tag,
            introduction = this.introduction,
            name = this.nickname,
            birth = this.birth,
            favoriteColorId = this.favoriteColorId
        )
    }
}

data class FriendInfo(
    var profileUrl: String? = "",
    val nickname: String = "",
    val tag: String = "",
    var introduction: String = "",
    val name: String,
    var birth: String = "",
    var favoriteColorId: Int = 0
)