package com.gradu.data.utils.mappers

import com.gradu.core.utils.converter.ScheduleDateConverter
import com.gradu.data.dto.FriendCategoryDTO
import com.gradu.data.dto.FriendDTO
import com.gradu.data.dto.FriendRequestDTO
import com.gradu.data.dto.GetFriendScheduleResult
import com.gradu.domain.model.CalendarColorInfo
import com.gradu.domain.model.Friend
import com.gradu.domain.model.FriendRequest
import com.gradu.domain.model.FriendSchedule
import com.gradu.domain.model.ScheduleCategoryInfo

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