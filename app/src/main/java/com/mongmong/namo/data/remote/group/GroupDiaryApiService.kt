package com.mongmong.namo.data.remote.group

import com.mongmong.namo.data.dto.DiaryResponse
import com.mongmong.namo.presentation.config.BaseResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface GroupDiaryApiService {
    // 모임 기록 활동 추가
    @Multipart
    @POST("group/diaries/{moimScheduleId}")
    suspend fun addMoimActivity(
        @Path("moimScheduleId") moimScheduleId: Long,
        @Query("activityName") activityName: String?,
        @Query("activityMoney") activityMoney: String?,
        @Query("participantUserIds") participantUserIds: List<Long>,
        @Part createImages: List<MultipartBody.Part>?
    ): DiaryResponse

    // 모임 기록 활동 수정
    @Multipart
    @PATCH("group/diaries/{activityId}")
    suspend fun editMoimActivity(
        @Path("activityId") activityId: Long,
        @Query("deleteImageIds") deleteImageIds: List<Long>?,
        @Query("activityName") activityName: String?,
        @Query("activityMoney") activityMoney: String?,
        @Query("participantUserIds") participantUserIds: List<Long>,
        @Part createImages: List<MultipartBody.Part>?
    ): DiaryResponse

    // 모임 기록 활동 삭제
    @DELETE("group/diaries/{activityId}")
    suspend fun deleteMoimActivity(
        @Path("activityId") moimActivityId: Long
    ): DiaryResponse

    // 모임 기록 삭제 (그룹에서 삭제)
    @DELETE("group/diaries/all/{moimScheduleId}")
    suspend fun deleteMoimDiary(
        @Path("moimScheduleId") moimScheduleId: Long
    ): BaseResponse

}