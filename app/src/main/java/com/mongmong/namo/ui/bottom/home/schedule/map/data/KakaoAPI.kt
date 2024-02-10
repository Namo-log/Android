package com.mongmong.namo.ui.bottom.home.schedule.map.data

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KakaoAPI {
    @GET("/v2/local/search/keyword.json")
    fun getSearchPlace(
        @Header("Authorization") headers : String,
        @Query("query") query : String,
        @Query("x") x : String,
        @Query("y") y : String,
        @Query("sort") sort : String
    ) : Call<ResultSearchPlace>
}