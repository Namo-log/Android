package com.mongmong.namo.data.remote.category

import android.util.Log
import com.mongmong.namo.data.remote.CategoryApiService
import com.mongmong.namo.domain.model.CategoryRequestBody
import com.mongmong.namo.domain.model.GetCategoryResponse
import com.mongmong.namo.presentation.config.ApplicationClass
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CategoryService(val view: CategoryDetailView) {
    val retrofitInterface = ApplicationClass.sRetrofit.create(CategoryApiService::class.java)

    fun tryPostCategory(body: CategoryRequestBody, id: Long) {
//        retrofitInterface.postCategory(body).enqueue(object : Callback<PostCategoryResponse> {
//
//            override fun onResponse(call: Call<PostCategoryResponse>, response: Response<PostCategoryResponse>) {
//                when(response.code()) {
//                    200 -> view.onPostCategorySuccess(response.body() as PostCategoryResponse, id)
//                    else -> view.onPostCategoryFailure(response.message())
//                }
//            }
//
//            override fun onFailure(call: Call<PostCategoryResponse>, t: Throwable) {
//                Log.d("PostCategory", "onFailure")
//                view.onPostCategoryFailure(t.message ?: "통신 오류")
//            }
//        })
    }

    fun tryPatchCategory(categoryId: Long, body: CategoryRequestBody, localId : Long) {
//        retrofitInterface.patchCategory(categoryId, body).enqueue(object : Callback<PostCategoryResponse> {
//
//            override fun onResponse(call: Call<PostCategoryResponse>, response: Response<PostCategoryResponse>) {
//                when(response.code()) {
//                    200 -> view.onPatchCategorySuccess(response.body() as PostCategoryResponse, localId)
//                    else -> view.onPatchCategoryFailure(response.message())
//                }
//            }
//
//            override fun onFailure(call: Call<PostCategoryResponse>, t: Throwable) {
//                Log.d("PatchCategory", "onFailure")
//                view.onPatchCategoryFailure(t.message ?: "통신 오류")
//            }
//        })
    }
}

class CategorySettingService(val view: CategorySettingView) {
    val retrofitInterface = ApplicationClass.sRetrofit.create(CategoryApiService::class.java)

    fun tryGetAllCategory() {
        retrofitInterface.getCategories().enqueue(object : Callback<GetCategoryResponse> {

            override fun onResponse(call: Call<GetCategoryResponse>, response: Response<GetCategoryResponse>) {
                when(response.code()) {
                    200 -> view.onGetAllCategorySuccess(response.body() as GetCategoryResponse)
                }
            }

            override fun onFailure(call: Call<GetCategoryResponse>, t: Throwable) {
                Log.d("GetCategory", "onFailure")
                view.onGetAllCategoryFailure(t.message ?: "통신 오류")
            }
        })
    }
}

class CategoryDeleteService(val view: CategoryDeleteView) {
    val retrofitInterface = ApplicationClass.sRetrofit.create(CategoryApiService::class.java)

    fun tryDeleteCategory(categoryId: Long, localId : Long) {
//        retrofitInterface.deleteCategory(categoryId).enqueue(object : Callback<BaseResponse> {
//
//            override fun onResponse(call: Call<BaseResponse>, response: Response<BaseResponse>) {
//                when(response.code()) {
//                    200 -> view.onDeleteCategorySuccess(response.body() as BaseResponse, localId)
//                }
//            }
//
//            override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
//                Log.d("GetCategory", "onFailure")
//                view.onDeleteCategoryFailure(t.message ?: "통신 오류")
//            }
//        })
    }
}