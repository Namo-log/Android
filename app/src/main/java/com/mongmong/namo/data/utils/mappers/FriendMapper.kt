package com.mongmong.namo.data.utils.mappers

import com.mongmong.namo.data.dto.FriendCategoryDTO
import com.mongmong.namo.data.dto.FriendDTO
import com.mongmong.namo.data.dto.FriendRequestDTO
import com.mongmong.namo.data.dto.GetFriendScheduleResult
import com.mongmong.namo.domain.model.CalendarColorInfo
import com.mongmong.namo.domain.model.Friend
import com.mongmong.namo.domain.model.FriendRequest
import com.mongmong.namo.domain.model.FriendSchedule
import com.mongmong.namo.domain.model.ScheduleCategoryInfo
import com.mongmong.namo.presentation.utils.converter.ScheduleDateConverter

object FriendMapper {
    fun FriendDTO.toModel(): Friend {
        return Friend(
            userId = this.memberId,
            profileUrl = this.profileImage,
            nickname = this.nickname,
            name = this.nickname,
            introduction = this.bio,
            isFavorite = this.favoriteFriend,
            birth = this.birthDay,
            favoriteColorId = this.favoriteColorId,
            tag = this.tag
        )
    }

    fun GetFriendScheduleResult.toModel(): FriendSchedule {
        return FriendSchedule(
            scheduleId = this.scheduleId,
            title = this.title,
            startDate = ScheduleDateConverter.parseServerDateToLocalDateTime(this.startDate),
            endDate = ScheduleDateConverter.parseServerDateToLocalDateTime(this.endDate),
            categoryInfo = ScheduleCategoryInfo(
                this.categoryInfo.categoryId,
                this.categoryInfo.colorId,
                this.categoryInfo.name
            )
        )
    }

    fun FriendCategoryDTO.toModel(): CalendarColorInfo {
        return CalendarColorInfo(
            colorId = this.colorId,
            name = this.categoryName
        )
    }

    fun FriendRequestDTO.toModel(): FriendRequest {
        return FriendRequest(
            userId = this.memberId,
            friendRequestId = this.friendRequestId,
            profileUrl = this.profileImage,
            nickname = this.nickname,
            tag = this.tag,
            introduction = this.bio,
            birth = this.birth,
            name = this.nickname,
            favoriteColorId = this.favoriteColorId
        )
    }
}