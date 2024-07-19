package com.mongmong.namo.data.remote

import com.mongmong.namo.domain.model.DiaryAddResponse
import com.mongmong.namo.domain.model.DiaryGetAllResponse
import com.mongmong.namo.domain.model.DiaryGetMonthResponse
import com.mongmong.namo.domain.model.DiaryResponse
import com.mongmong.namo.domain.model.DiarySchedule
import com.mongmong.namo.domain.model.GetMoimMemoResponse
import com.mongmong.namo.domain.model.GetPersonalDiaryResponse
import com.mongmong.namo.domain.model.group.GetMoimDiaryResponse
import retrofit2.Call
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface DiaryApiService {
    /** 개인 */
    // 개인 기록 전체 조회
    @GET("diaries/all")
    fun getAllDiary(): Call<DiaryGetAllResponse>

    // 개인 기록 월별 조회
    @GET("diaries/month/{month}")
    suspend fun getPersonalMonthDiary(
        @Path("month") month: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): DiaryGetMonthResponse

    // 개인 기록 개별 조회
    @GET("diaries/{scheduleId}")
    suspend fun getPersonalDiary(
        @Path("scheduleId") scheduleId: Long
    ): GetPersonalDiaryResponse

    // 개인 기록 추가
    @Multipart
    @POST("diaries")
    suspend fun addPersonalDiary(
        @Part("scheduleId") scheduleId: RequestBody,
        @Part("content") content: RequestBody?,
        @Part imgs: List<MultipartBody.Part>?
    ): DiaryAddResponse

    // 개인 기록 수정
    @Multipart
    @PATCH("diaries")
    suspend fun editPersonalDiary(
        @Part("scheduleId") scheduleId: RequestBody,
        @Part("content") content: RequestBody?,
        @Part createImages: List<MultipartBody.Part>?,
        @Part("deleteImagesIds") deleteImagesIds: List<Int>?
    ): DiaryResponse

    // 개인 기록 삭제
    @DELETE("diaries/{scheduleId}")
    suspend fun deletePersonalDiary(
        @Path("scheduleId") scheduleId: Long
    ): DiaryResponse

    /** 모임 */
    // 월별 모임 기록 조회
    @GET("group/diaries/month/{month}")
    suspend fun getGroupMonthDiary(
        @Path("month") month: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): DiaryGetMonthResponse

    // 모임 기록 개별 조회
    @GET("group/diaries/{moimScheduleId}")
    suspend fun getMoimDiary(
        @Path("moimScheduleId") scheduleId: Long
    ): GetMoimDiaryResponse

    // 모임 메모 개별 조회
    @GET("group/diaries/detail/{moimScheduleId}")
    suspend fun getMoimMemo(
        @Path("moimScheduleId") moimScheduleId: Long
    ): GetMoimMemoResponse

    // 모임 메모 추가 or 수정
    @PATCH("group/diaries/text/{scheduleId}")
    suspend fun patchMoimMemo(
        @Path("scheduleId") scheduleId: Long,
        @Body text: String?
    ): DiaryResponse

    // 모임 메모 삭제 (개인)
    @DELETE("group/diaries/person/{scheduleId}")
    suspend fun deleteMoimMemo(
        @Path("scheduleId") scheduleId: Long,
    ): DiaryResponse
}

