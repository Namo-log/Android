package com.example.namo.data.remote.diary

import android.util.Log
import com.example.namo.config.ApplicationClass
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class DiaryService {
    private val diaryRetrofitInterface: DiaryInterface =
        ApplicationClass.bRetrofit.create(DiaryInterface::class.java)

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
        imgs: List<MultipartBody.Part?>?,
        content: RequestBody?,
        scheduleIdx: RequestBody
    ) {
        diaryRetrofitInterface.addDiary(imgs, content, scheduleIdx)
            .enqueue(object : Callback<DiaryResponse.DiaryAddResponse> {

                override fun onResponse(
                    call: Call<DiaryResponse.DiaryAddResponse>,
                    response: Response<DiaryResponse.DiaryAddResponse>
                ) {
                    val resp: DiaryResponse.DiaryAddResponse? = response.body()
                    if (resp != null) {
                        when (val code = resp.code) {
                            200 -> diaryView.onAddDiarySuccess(code, resp.message, resp.result)
                        }
                    }
                }

                override fun onFailure(call: Call<DiaryResponse.DiaryAddResponse>, t: Throwable) {
                    Log.d("addDiary/failure", t.message.toString())
                }
            })
    }


    /** 기록 수정 **/
    fun editDiary(
        imgs: List<MultipartBody.Part?>?,
        content: RequestBody?,
        scheduleIdx: RequestBody
    ) {
        diaryRetrofitInterface.editDiary(imgs, content, scheduleIdx)
            .enqueue(object : Callback<DiaryResponse.DiaryEditResponse> {

                override fun onResponse(
                    call: Call<DiaryResponse.DiaryEditResponse>,
                    response: Response<DiaryResponse.DiaryEditResponse>
                ) {
                    val resp: DiaryResponse.DiaryEditResponse? = response.body()
                    if (resp != null) {
                        when (val code = resp.code) {
                            200 -> diaryDetailView.onEditDiarySuccess(code, resp.message, resp.result)
                        }
                    }
                }

                override fun onFailure(call: Call<DiaryResponse.DiaryEditResponse>, t: Throwable) {
                    Log.d("editDiary/failure", t.message.toString())
                }
            })
    }


    /** 기록 삭제 **/
    fun deleteDiary(
        scheduleIdx: Int
    ) {
        diaryRetrofitInterface.deleteDiary(scheduleIdx)
            .enqueue(object : Callback<DiaryResponse.DiaryDeleteResponse> {

                override fun onResponse(
                    call: Call<DiaryResponse.DiaryDeleteResponse>,
                    response: Response<DiaryResponse.DiaryDeleteResponse>
                ) {
                    val resp: DiaryResponse.DiaryDeleteResponse? = response.body()
                    if (resp != null) {
                        when (val code = resp.code) {
                            200 -> diaryDetailView.onDeleteDiarySuccess(code, resp.message, resp.result)
                        }
                    }
                }

                override fun onFailure(
                    call: Call<DiaryResponse.DiaryDeleteResponse>,
                    t: Throwable
                ) {
                    Log.d("deleteDiary/failure", t.message.toString())
                }
            })
    }


    /** 기록 월 별 조회 **/
    fun getMonthDiary(
        month: String
    ) {
        diaryRetrofitInterface.getMonthDiary(month)
            .enqueue(object : Callback<DiaryResponse.DiaryGetMonthResponse> {

                override fun onResponse(
                    call: Call<DiaryResponse.DiaryGetMonthResponse>,
                    response: Response<DiaryResponse.DiaryGetMonthResponse>
                ) {
                    val resp: DiaryResponse.DiaryGetMonthResponse? = response.body()
                    if (resp != null) {
                        when (val code = resp.code) {
                            200 -> getMonthDiaryView.onGetMonthDiarySuccess(
                                code,
                                resp.message,
                                resp.result
                            )
                        }
                    }
                }

                override fun onFailure(
                    call: Call<DiaryResponse.DiaryGetMonthResponse>,
                    t: Throwable
                ) {
                    Log.d("getMonthDiary/failure", t.message.toString())
                }
            })
    }

    /** 기록 일 별 조회 **/
    fun getDayDiary(
        scheduleIdx: Int
    ) {
        diaryRetrofitInterface.getDayDiary(scheduleIdx)
            .enqueue(object : Callback<DiaryResponse.DiaryGetDayResponse> {

                override fun onResponse(
                    call: Call<DiaryResponse.DiaryGetDayResponse>,
                    response: Response<DiaryResponse.DiaryGetDayResponse>
                ) {
                    val resp: DiaryResponse.DiaryGetDayResponse? = response.body()
                    if (resp != null) {
                        when (val code = resp.code) {
                            200 -> getDayDiaryView.onGetDayDiarySuccess(code, resp.message, resp.result)
                        }
                    }
                }

                override fun onFailure(
                    call: Call<DiaryResponse.DiaryGetDayResponse>,
                    t: Throwable
                ) {
                    Log.d("getDayDiary/failure", t.message.toString())
                }
            })
    }


}



