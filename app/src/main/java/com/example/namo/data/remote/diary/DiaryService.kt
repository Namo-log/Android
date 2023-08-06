package com.example.namo.data.remote.diary

import android.annotation.SuppressLint
import com.example.namo.config.ApplicationClass
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class DiaryService {
    private val diaryRetrofitInterface: DiaryInterface =
        ApplicationClass.sRetrofit.create(DiaryInterface::class.java)

    private lateinit var diaryView: DiaryView
    private lateinit var diaryDetailView: DiaryDetailView
    private lateinit var getMonthDiaryView: GetMonthDiaryView
    private lateinit var getDayDiaryView: GetDayDiaryView

    fun addDiaryView(diaryView: DiaryView) {
        this.diaryView = diaryView
    }

    fun setDiaryView(diaryDetailView: DiaryDetailView) {
        this.diaryDetailView = diaryDetailView
    }

    fun getMonthDiaryView(getMonthDiaryView: GetMonthDiaryView) {
        this.getMonthDiaryView = getMonthDiaryView
    }

    fun getDayDiaryView(getDayDiaryView: GetDayDiaryView) {
        this.getDayDiaryView = getDayDiaryView
    }

    /** 기록 추가 **/
    fun addDiary(
        localId: Long,
        images: List<MultipartBody.Part>?,
        content: RequestBody?,
        scheduleIdx: RequestBody
    ) {
        diaryRetrofitInterface.addDiary(scheduleIdx, content, images)
            .enqueue(object : Callback<DiaryResponse.DiaryAddResponse> {

                override fun onResponse(
                    call: Call<DiaryResponse.DiaryAddResponse>,
                    response: Response<DiaryResponse.DiaryAddResponse>
                ) {
                    val resp: DiaryResponse.DiaryAddResponse? = response.body()
                    when (response.code()) {

                        200 -> if (resp != null) {
                            diaryView.onAddDiarySuccess(
                                resp, localId
                            )
                        }
                        else -> diaryView.onAddDiaryFailure(localId, response.toString())
                    }
                }

                override fun onFailure(call: Call<DiaryResponse.DiaryAddResponse>, t: Throwable) {
                    diaryView.onAddDiaryFailure(localId, t.message.toString())
                }
            })
    }


    /** 기록 수정 **/
    fun editDiary(
        localId: Long,
        images: List<MultipartBody.Part>?,
        content: RequestBody?,
        scheduleIdx: RequestBody
    ) {
        diaryRetrofitInterface.editDiary(scheduleIdx, content, images)
            .enqueue(object : Callback<DiaryResponse.DiaryEditResponse> {

                @SuppressLint("SuspiciousIndentation")
                override fun onResponse(
                    call: Call<DiaryResponse.DiaryEditResponse>,
                    response: Response<DiaryResponse.DiaryEditResponse>
                ) {
                    val resp: DiaryResponse.DiaryEditResponse? = response.body()
                    when (response.code()) {
                        200 -> if (resp != null) {
                            diaryDetailView.onEditDiarySuccess(
                                resp,
                                localId
                            )
                        }
                        else -> diaryDetailView.onEditDiaryFailure(localId, response.toString())

                    }
                }

                override fun onFailure(call: Call<DiaryResponse.DiaryEditResponse>, t: Throwable) {
                    diaryDetailView.onEditDiaryFailure(localId, t.message.toString())
                }
            })
    }


    /** 기록 삭제 **/
    fun deleteDiary(
        localId: Long,
        scheduleIdx: Long
    ) {
        diaryRetrofitInterface.deleteDiary(scheduleIdx)
            .enqueue(object : Callback<DiaryResponse.DiaryDeleteResponse> {

                @SuppressLint("SuspiciousIndentation")
                override fun onResponse(
                    call: Call<DiaryResponse.DiaryDeleteResponse>,
                    response: Response<DiaryResponse.DiaryDeleteResponse>
                ) {
                    val resp: DiaryResponse.DiaryDeleteResponse? = response.body()
                    when (response.code()) {
                        200 -> if (resp != null) {
                            diaryDetailView.onDeleteDiarySuccess(
                                resp, localId
                            )
                        }
                        else -> diaryDetailView.onDeleteDiaryFailure(
                            localId,
                            response.code().toString()
                        )
                    }
                }

                override fun onFailure(
                    call: Call<DiaryResponse.DiaryDeleteResponse>,
                    t: Throwable
                ) {
                    diaryDetailView.onDeleteDiaryFailure(localId, t.message.toString())
                }
            })
    }


    /** 기록 월 별 조회 **/
    fun getMonthDiary(
        month: String,
        page: Int,
        size: Int
    ) {
        diaryRetrofitInterface.getMonthDiary(month, page, size)
            .enqueue(object : Callback<DiaryResponse.DiaryGetMonthResponse> {

                @SuppressLint("SuspiciousIndentation")
                override fun onResponse(
                    call: Call<DiaryResponse.DiaryGetMonthResponse>,
                    response: Response<DiaryResponse.DiaryGetMonthResponse>
                ) {
                    val resp: DiaryResponse.DiaryGetMonthResponse? = response.body()
                    when (response.code()) {
                        200 -> if (resp != null) {
                            getMonthDiaryView.onGetMonthDiarySuccess(
                                resp
                            )
                        }
                        else -> getMonthDiaryView.onGetMonthDiaryFailure(
                            month,
                            response.toString(),
                            page,
                            size
                        )
                    }

                }

                override fun onFailure(
                    call: Call<DiaryResponse.DiaryGetMonthResponse>,
                    t: Throwable
                ) {
                    getMonthDiaryView.onGetMonthDiaryFailure(
                        month,
                        t.message.toString(),
                        page,
                        size
                    )
                }
            })
    }

    /** 기록 일 별 조회 **/
    fun getDayDiary(
        localId: Long,
        scheduleIdx: Long,
    ) {
        diaryRetrofitInterface.getDayDiary(scheduleIdx)
            .enqueue(object : Callback<DiaryResponse.DiaryGetDayResponse> {

                @SuppressLint("SuspiciousIndentation")
                override fun onResponse(
                    call: Call<DiaryResponse.DiaryGetDayResponse>,
                    response: Response<DiaryResponse.DiaryGetDayResponse>
                ) {
                    val resp: DiaryResponse.DiaryGetDayResponse? = response.body()
                    when (response.code()) {
                        200 -> resp?.result?.let {
                            getDayDiaryView.onGetDayDiarySuccess(
                                resp, scheduleIdx
                            )
                        }
                        else ->
                            getDayDiaryView.onGetDayDiaryFailure(localId, response.toString())
                    }

                }

                override fun onFailure(
                    call: Call<DiaryResponse.DiaryGetDayResponse>,
                    t: Throwable
                ) {
                    getDayDiaryView.onGetDayDiaryFailure(localId, t.message.toString())
                }
            })
    }
}



