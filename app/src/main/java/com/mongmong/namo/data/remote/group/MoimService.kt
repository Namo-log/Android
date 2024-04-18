package com.mongmong.namo.data.remote.group

import android.util.Log
import com.mongmong.namo.presentation.config.ApplicationClass
import com.mongmong.namo.presentation.config.BaseResponse
import com.mongmong.namo.domain.model.GetMoimScheduleResponse
import com.mongmong.namo.domain.model.MoimScheduleAlarmBody
import com.mongmong.namo.domain.model.PatchMoimScheduleCategoryBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MoimService {
    private val moimRetrofitInterface : GroupApiService =
        ApplicationClass.sRetrofit.create(GroupApiService::class.java)

    private lateinit var getMoimScheduleView : GetMoimScheduleView
    private lateinit var editMoimScheduleView : EditMoimScheduleView


    fun setGetMoimScheduleView(getMoimScheduleView: GetMoimScheduleView) {
        this.getMoimScheduleView = getMoimScheduleView
    }

    fun setEditMoimScheduleView (editMoimScheduleView: EditMoimScheduleView) {
        this.editMoimScheduleView = editMoimScheduleView
    }

    fun getMoimSchedule(
        moimId : Long,
        yearMonth : String
    ) {
        moimRetrofitInterface.getMonthMoimSchedule(moimId, yearMonth)
            .enqueue(object : Callback<GetMoimScheduleResponse> {
                override fun onResponse(
                    call: Call<GetMoimScheduleResponse>,
                    response: Response<GetMoimScheduleResponse>
                ) {
                    when(response.code()) {
                        200 -> getMoimScheduleView.onGetMoimScheduleSuccess(response.body() as GetMoimScheduleResponse)
                        else -> {
                            Log.d("GetMoimSchedule", "Success but error")
                            getMoimScheduleView.onGetMoimScheduleFailure("통신 중 200 외 기타 코드")
                        }
                    }
                }

                override fun onFailure(call: Call<GetMoimScheduleResponse>, t: Throwable) {
                    Log.d("GetMoimSchedule", "onFailure")
                    getMoimScheduleView.onGetMoimScheduleFailure(t.message ?: "통신 오류")
                }

            })
    }
}