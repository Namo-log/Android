package com.mongmong.namo.data.remote.diary

import android.annotation.SuppressLint
import com.mongmong.namo.domain.model.DiaryAddResponse
import com.mongmong.namo.domain.model.DiaryGetAllResponse
import com.mongmong.namo.domain.model.DiaryGetMonthResponse
import com.mongmong.namo.domain.model.DiaryResponse
import com.mongmong.namo.domain.model.GetGroupDiaryResponse
import com.mongmong.namo.presentation.config.ApplicationClass
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class DiaryService {
    private val diaryRetrofitInterface: DiaryApiService =
        ApplicationClass.sRetrofit.create(DiaryApiService::class.java)

    private lateinit var diaryDetailView: DiaryDetailView
    private lateinit var getMonthDiaryView: GetMonthDiaryView
    private lateinit var getGroupDiaryView: GetGroupDiaryView
    private lateinit var getGroupMonthView: GetGroupMonthView
    private lateinit var addGroupAfterDiaryView: AddGroupAfterDiaryView

    fun setDiaryView(diaryDetailView: DiaryDetailView) {
        this.diaryDetailView = diaryDetailView
    }

    fun getMonthDiaryView(getMonthDiaryView: GetMonthDiaryView) {
        this.getMonthDiaryView = getMonthDiaryView
    }

    fun getGroupDiaryView(getGroupDiaryView: GetGroupDiaryView) {
        this.getGroupDiaryView = getGroupDiaryView
    }

    fun addGroupAfterDiary(addGroupAfterDiaryView: AddGroupAfterDiaryView) {
        this.addGroupAfterDiaryView = addGroupAfterDiaryView
    }

    fun getGroupMonthView(getGroupMonthView: GetGroupMonthView) {
        this.getGroupMonthView = getGroupMonthView
    }

    /** 기록 추가 **/
    fun addDiary(
        localId: Long,
        images: List<MultipartBody.Part>?,
        content: RequestBody?,
        scheduleIdx: RequestBody,
        callback: AddPersonalDiaryView
    ) {
        /*diaryRetrofitInterface.addDiary(scheduleIdx, content, images)
            .enqueue(object : Callback<DiaryAddResponse> {

                override fun onResponse(
                    call: Call<DiaryAddResponse>,
                    response: Response<DiaryAddResponse>
                ) {
                    val resp: DiaryAddResponse? = response.body()
                    when (response.code()) {

                        200 -> if (resp != null) {
                            callback.onAddDiarySuccess(
                                resp, localId
                            )
                        }
                        else -> callback.onAddDiaryFailure(response.toString())
                    }
                }

                override fun onFailure(call: Call<DiaryAddResponse>, t: Throwable) {
                    callback.onAddDiaryFailure(t.message.toString())
                }
            })*/
    }


    /** 기록 수정 **/
    fun editDiary(
        localId: Long,
        serverId: Long,
        images: List<MultipartBody.Part>?,
        content: RequestBody?,
        scheduleIdx: RequestBody,
    ) {
        /*diaryRetrofitInterface.editDiary(scheduleIdx, content, images)
            .enqueue(object : Callback<DiaryResponse> {

                @SuppressLint("SuspiciousIndentation")
                override fun onResponse(
                    call: Call<DiaryResponse>,
                    response: Response<DiaryResponse>
                ) {
                    val resp: DiaryResponse? = response.body()
                    when (response.code()) {
                        200 -> if (resp != null) {
                            diaryDetailView.onEditDiarySuccess(
                                resp,
                                localId, serverId
                            )
                        }
                        else -> diaryDetailView.onEditDiaryFailure(response.toString())

                    }
                }

                override fun onFailure(call: Call<DiaryResponse>, t: Throwable) {
                    diaryDetailView.onEditDiaryFailure(t.message.toString())
                }
            })*/
    }


    /** 기록 삭제 **/
    fun deleteDiary(
        localId: Long,
        scheduleIdx: Long
    ) {
        diaryRetrofitInterface.deleteDiary(scheduleIdx)
            .enqueue(object : Callback<DiaryResponse> {

                @SuppressLint("SuspiciousIndentation")
                override fun onResponse(
                    call: Call<DiaryResponse>,
                    response: Response<DiaryResponse>
                ) {
                    val resp: DiaryResponse? = response.body()
                    when (response.code()) {
                        200 -> if (resp != null) {
                            diaryDetailView.onDeleteDiarySuccess(
                                resp, localId
                            )
                        }
                        else -> diaryDetailView.onDeleteDiaryFailure(
                            response.code().toString()
                        )
                    }
                }

                override fun onFailure(
                    call: Call<DiaryResponse>,
                    t: Throwable
                ) {
                    diaryDetailView.onDeleteDiaryFailure(t.message.toString())
                }
            })
    }


    /** 기록 월 별 조회 **/
    fun getAllDiary() {
        diaryRetrofitInterface.getAllDiary()
            .enqueue(object : Callback<DiaryGetAllResponse> {

                @SuppressLint("SuspiciousIndentation")
                override fun onResponse(
                    call: Call<DiaryGetAllResponse>,
                    response: Response<DiaryGetAllResponse>
                ) {
                    val resp: DiaryGetAllResponse? = response.body()
                    when (response.code()) {
                        200 -> if (resp != null) {
                            getMonthDiaryView.onGetMonthDiarySuccess(
                                resp
                            )
                        }
                        else -> getMonthDiaryView.onGetMonthDiaryFailure(
                            response.toString()
                        )
                    }

                }

                override fun onFailure(
                    call: Call<DiaryGetAllResponse>,
                    t: Throwable
                ) {
                    getMonthDiaryView.onGetMonthDiaryFailure(
                        t.message.toString()
                    )
                }
            })
    }


    /** 그룹 메모 추가 **/

    fun addGroupDiary(
        moimScheduleIdx: Long,
        name: RequestBody,
        money: RequestBody,
        members: RequestBody?,
        imgs: List<MultipartBody.Part>?,
        callback: DiaryBasicView
    ) {
        try {
            val response = diaryRetrofitInterface.addGroupDiary(
                moimScheduleIdx,
                name,
                money,
                members,
                imgs
            ).execute()

            if (response.isSuccessful) {
                val resp = response.body()
                when (response.code()) {
                    200 -> resp?.let { callback.onSuccess(resp) }
                    else -> callback.onFailure(response.toString())
                }
            } else {
                callback.onFailure(response.toString())
            }
        } catch (e: Exception) {
            callback.onFailure(e.message ?: "Unknown error occurred")
        }
    }


    /** 그룹 기록 일 별 조회 **/
    fun getGroupDiary(
        moimScheduleIdx: Long
    ) {
        diaryRetrofitInterface.getGroupDiary(moimScheduleIdx)
            .enqueue(object : Callback<GetGroupDiaryResponse> {

                @SuppressLint("SuspiciousIndentation")
                override fun onResponse(
                    call: Call<GetGroupDiaryResponse>,
                    response: Response<GetGroupDiaryResponse>
                ) {
                    val resp: GetGroupDiaryResponse? = response.body()
                    when (response.code()) {
                        200 -> if (resp != null) {
                            getGroupDiaryView.onGetGroupDiarySuccess(resp)
                        }
                        else ->
                            getGroupDiaryView.onGetGroupDiaryFailure(response.toString())
                    }

                }

                override fun onFailure(
                    call: Call<GetGroupDiaryResponse>,
                    t: Throwable
                ) {
                    getGroupDiaryView.onGetGroupDiaryFailure(t.message.toString())
                }
            })
    }


    /** 그룹 메모 수정 **/
    fun editGroupDiary(
        moimScheduleIdx: Long,
        name: RequestBody,
        money: RequestBody,
        members: RequestBody?,
        imgs: List<MultipartBody.Part>?,
        callback: DiaryBasicView
    ) {
        diaryRetrofitInterface.patchGroupDiaryPlace(moimScheduleIdx, name, money, members, imgs)
            .enqueue(object : Callback<DiaryResponse> {

                @SuppressLint("SuspiciousIndentation")
                override fun onResponse(
                    call: Call<DiaryResponse>,
                    response: Response<DiaryResponse>
                ) {
                    val resp: DiaryResponse? = response.body()
                    when (response.code()) {
                        200 -> if (resp != null) {
                            callback.onSuccess(resp)
                        }
                        else ->
                            callback.onFailure(response.message())
                    }

                }

                override fun onFailure(
                    call: Call<DiaryResponse>,
                    t: Throwable
                ) {
                    callback.onFailure(t.message.toString())
                }
            })
    }

    fun deleteGroupDiary(
        moimScheduleIdx: Long,
        callback: DiaryBasicView
    ) {
        try {
            val response = diaryRetrofitInterface.deleteGroupDiaryPlace(moimScheduleIdx).execute()

            if (response.isSuccessful) {
                val resp = response.body()
                when (response.code()) {
                    200 -> resp?.let { callback.onSuccess(resp) }
                    else -> callback.onFailure(response.toString())
                }
            } else {
                callback.onFailure(response.toString())
            }
        } catch (e: Exception) {
            callback.onFailure(e.message ?: "Unknown error occurred")
        }
    }

    suspend fun getGroupMonthDiary(
        month: String,
        page: Int,
        size: Int
    ): DiaryGetMonthResponse {
        return diaryRetrofitInterface.getGroupMonthDiary(month, page, size)
    }

    fun getGroupMonthDiary2(
        month: String,
        page: Int,
        size: Int
    ) {
        diaryRetrofitInterface.getGroupMonthDiary2(month, page, size)
            .enqueue(object : Callback<DiaryGetMonthResponse> {

                @SuppressLint("SuspiciousIndentation")
                override fun onResponse(
                    call: Call<DiaryGetMonthResponse>,
                    response: Response<DiaryGetMonthResponse>
                ) {
                    val resp: DiaryGetMonthResponse? = response.body()
                    when (response.code()) {
                        200 -> if (resp != null) {
                            getGroupMonthView.onGetGroupMonthSuccess(resp)
                        }
                        else -> getGroupMonthView.onGetGroupMonthFailure(response.toString())
                    }

                }

                override fun onFailure(
                    call: Call<DiaryGetMonthResponse>,
                    t: Throwable
                ) {
                    getGroupMonthView.onGetGroupMonthFailure(t.message.toString())
                }
            })
    }

    fun addGroupAfterDiary(
        groupScheduleIdx: Long,
        content: String?
    ) {
        diaryRetrofitInterface.addGroupAfterDiary(groupScheduleIdx, content)
            .enqueue(object : Callback<DiaryResponse> {

                @SuppressLint("SuspiciousIndentation")
                override fun onResponse(
                    call: Call<DiaryResponse>,
                    response: Response<DiaryResponse>
                ) {
                    val resp: DiaryResponse? = response.body()
                    when (response.code()) {
                        200 -> if (resp != null) {
                            addGroupAfterDiaryView.onAddGroupAfterDiarySuccess(resp)
                        }
                        else -> addGroupAfterDiaryView.onAddGroupAfterDiaryFailure(response.toString())
                    }

                }

                override fun onFailure(
                    call: Call<DiaryResponse>,
                    t: Throwable
                ) {
                    addGroupAfterDiaryView.onAddGroupAfterDiaryFailure(t.message.toString())
                }
            })
    }
}



