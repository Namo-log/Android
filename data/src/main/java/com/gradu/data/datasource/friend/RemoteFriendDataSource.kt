package com.gradu.data.datasource.friend

import android.util.Log
import com.mongmong.namo.data.dto.FriendBaseResponse
import com.mongmong.namo.data.dto.GetFriendCategoryResponse
import com.mongmong.namo.data.dto.GetFriendListResponse
import com.mongmong.namo.data.dto.GetFriendListResult
import com.mongmong.namo.data.dto.GetFriendRequestResponse
import com.mongmong.namo.data.dto.GetFriendRequestResult
import com.mongmong.namo.data.dto.GetFriendScheduleResponse
import com.mongmong.namo.data.remote.FriendApiService
import com.mongmong.namo.domain.model.BaseResponse
import com.mongmong.namo.presentation.utils.converter.ScheduleDateConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import javax.inject.Inject

class RemoteFriendDataSource @Inject constructor(
    private val friendApiService: FriendApiService
) {
    /** 친구 정보 */
    // 친구 목록 조회
    suspend fun getFriends(): GetFriendListResponse {
        var friendResponse = GetFriendListResponse(result = GetFriendListResult())
        withContext(Dispatchers.IO) {
            runCatching {
                friendApiService.getFriendList(searchWord = null) //TODO: 페이지, 친구 검색
            }.onSuccess {
                Log.d("RemoteFriendDataSource", "getFriends Success $it")
                friendResponse = it
            }.onFailure {
                Log.d("RemoteFriendDataSource", "getFriends Failure $it")
            }
        }
        return friendResponse
    }

    // 친구 캘린더 조회
    suspend fun getFriendMonthSchedules(
        startDate: DateTime,
        endDate: DateTime,
        userId: Long
    ): GetFriendScheduleResponse {
        var scheduleResponse = GetFriendScheduleResponse(result = emptyList())
        withContext(Dispatchers.IO) {
            runCatching {
                friendApiService.getFriendMonthSchedules(
                    startDate = ScheduleDateConverter.parseDateTimeToServerData(startDate),
                    endDate = ScheduleDateConverter.parseDateTimeToServerData(endDate),
                    userId = userId
                )
            }.onSuccess {
                Log.d("RemoteFriendDataSource", "getMonthSchedules Success $it")
                scheduleResponse = it
            }.onFailure {
                Log.d("RemoteFriendDataSource", "getMonthSchedules Success $it")
            }
        }
        return scheduleResponse
    }

    // 친구 캘린더 카테고리 조회
    suspend fun getFriendCategories(
        userId: Long
    ): GetFriendCategoryResponse {
        var categoryResponse = GetFriendCategoryResponse(result = emptyList())
        withContext(Dispatchers.IO) {
            runCatching {
                friendApiService.getFriendCategories(
                    userId = userId
                )
            }.onSuccess {
                Log.d("RemoteFriendDataSource", "getFriendCategories Success $it")
                categoryResponse = it
            }.onFailure {
                Log.d("RemoteFriendDataSource", "getFriendCategories Success $it")
            }
        }
        return categoryResponse
    }

    // 친구 삭제
    suspend fun deleteFriend(
        userId: Long
    ): BaseResponse {
        var friendResponse = BaseResponse()
        withContext(Dispatchers.IO) {
            runCatching {
                friendApiService.deleteFriend(
                    userId = userId
                )
            }.onSuccess {
                Log.d("RemoteFriendDataSource", "deleteFriend Success $it")
                friendResponse = it
            }.onFailure {
                Log.d("RemoteFriendDataSource", "deleteFriend Success $it")
            }
        }
        return friendResponse
    }

    // 친구 즐겨찾기 등록/해제
    suspend fun toggleFriendFavoriteState(
        userId: Long
    ): BaseResponse {
        var friendResponse = BaseResponse()
        withContext(Dispatchers.IO) {
            runCatching {
                friendApiService.toggleFriendFavoriteState(
                    userId = userId
                )
            }.onSuccess {
                Log.d("RemoteFriendDataSource", "toggleFriendFavoriteState Success $it")
                friendResponse = it
            }.onFailure {
                Log.d("RemoteFriendDataSource", "toggleFriendFavoriteState Success $it")
            }
        }
        return friendResponse
    }

    /** 친구 요청 */
    // 친구 요청 목록 조회
    suspend fun getFriendRequests(): GetFriendRequestResponse {
        var friendResponse = GetFriendRequestResponse(result = GetFriendRequestResult())
        withContext(Dispatchers.IO) {
            runCatching {
                friendApiService.getFriendRequestList()
            }.onSuccess {
                Log.d("RemoteFriendDataSource", "getFriendRequests Success $it")
                friendResponse = it
            }.onFailure {
                Log.d("RemoteFriendDataSource", "getFriendRequests Failure $it")
            }
        }
        return friendResponse
    }

    // 친구 요청
    suspend fun postFriendRequest(
        nicknameTag: String
    ): FriendBaseResponse {
        var friendResponse = FriendBaseResponse()
        withContext(Dispatchers.IO) {
            runCatching {
                friendApiService.postFriendRequest(nicknameTag)
            }.onSuccess {
                Log.d("RemoteFriendDataSource", "postFriendRequest Success $it")
                friendResponse = it
            }.onFailure {
                Log.d("RemoteFriendDataSource", "postFriendRequest Failure $it")
            }
        }
        return friendResponse
    }

    // 친구 요청 수락
    suspend fun acceptFriendRequest(
        requestId: Long
    ): FriendBaseResponse {
        var friendResponse = FriendBaseResponse()
        withContext(Dispatchers.IO) {
            runCatching {
                friendApiService.acceptFriendRequest(requestId)
            }.onSuccess {
                Log.d("RemoteFriendDataSource", "acceptFriendRequest Success $it")
                friendResponse = it
            }.onFailure {
                Log.d("RemoteFriendDataSource", "acceptFriendRequest Failure $it")
            }
        }
        return friendResponse
    }

    // 친구 요청 거절
    suspend fun rejectFriendRequest(
        requestId: Long
    ): FriendBaseResponse {
        var friendResponse = FriendBaseResponse()
        withContext(Dispatchers.IO) {
            runCatching {
                friendApiService.rejectFriendRequest(requestId)
            }.onSuccess {
                Log.d("RemoteFriendDataSource", "rejectFriendRequest Success $it")
                friendResponse = it
            }.onFailure {
                Log.d("RemoteFriendDataSource", "rejectFriendRequest Failure $it")
            }
        }
        return friendResponse
    }
}