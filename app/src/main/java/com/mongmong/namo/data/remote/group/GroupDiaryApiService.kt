package com.mongmong.namo.data.remote.group

import com.mongmong.namo.domain.model.DiaryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface GroupDiaryApiService {
    // 모임 기록 활동 추가
    @Multipart
    @POST("/moims/schedule/memo/{moimId}")
    suspend fun addMoimDiary(
        @Path("moimId") scheduleId: Long,
        @Part("name") place: RequestBody?,
        @Part("money") pay: RequestBody?,
        @Part("participants") member: RequestBody?,
        @Part imgs: List<MultipartBody.Part>?
    ): DiaryResponse

    // 모임 기록 활동 수정
    @Multipart
    @PATCH("/moims/schedule/memo/{moimMemoLocationId}")
    suspend fun editMoimActivity(
        @Path("moimMemoLocationId") moimScheduldId: Long,
        @Part("name") place: RequestBody?,
        @Part("money") pay: RequestBody?,
        @Part("participants") member: RequestBody?,
        @Part imgs: List<MultipartBody.Part>?
    ): DiaryResponse

    // 모임 기록 활동 삭제
    @DELETE("/moims/schedule/memo/{moimMemoLocationId}")
    suspend fun deleteMoimActivity(
        @Path("moimMemoLocationId") moimActivityId: Long
    ): DiaryResponse
}