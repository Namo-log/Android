package com.example.namo.data.remote.category

import android.util.Log
import com.example.namo.config.ApplicationClass
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CategoryService(val view: CategoryDetailView) {
    val retrofitInterface = ApplicationClass.sRetrofit.create(CategoryRetrofitInterface::class.java)

    fun tryPostCategory(body: CategoryBody) {
        retrofitInterface.postCategory(body).enqueue(object : Callback<PostCategoryResponse> {

            override fun onResponse(call: Call<PostCategoryResponse>, response: Response<PostCategoryResponse>) {
                when(response.code()) {
                    200 -> view.onPostCategorySuccess(response.body() as PostCategoryResponse)
                }
            }

            override fun onFailure(call: Call<PostCategoryResponse>, t: Throwable) {
                Log.d("PostCategory", "onFailure")
                view.onPostCategoryFailure(t.message ?: "통신 오류")
            }
        })
    }

    fun tryPatchCategory(categoryId: Int, body: CategoryBody) {
        retrofitInterface.patchCategory(categoryId, body).enqueue(object : Callback<PostCategoryResponse> {

            override fun onResponse(call: Call<PostCategoryResponse>, response: Response<PostCategoryResponse>) {
                when(response.code()) {
                    200 -> view.onPostCategorySuccess(response.body() as PostCategoryResponse)
                }
            }

            override fun onFailure(call: Call<PostCategoryResponse>, t: Throwable) {
                Log.d("PostCategory", "onFailure")
                view.onPostCategoryFailure(t.message ?: "통신 오류")
            }
        })
    }
}