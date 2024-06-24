package com.mongmong.namo.presentation.ui.home.schedule.map.data

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KakaoAPI {
    /** 키워드로 장소 검색 */
    @GET("/v2/local/search/keyword.json")
    fun getSearchPlace(
        @Header("Authorization") headers : String,
        @Query("query") query : String,
        @Query("x") x : String,
        @Query("y") y : String,
        @Query("sort") sort : String
    ) : Call<ResultSearchPlace>

    /** 좌표로 주소 변환 */
    @GET("/v2/local/geo/coord2address.json")
    fun getPlaceInfo(
        @Header("Authorization") headers: String,
        @Query("x") longitude: String,
        @Query("y") latitude: String
    ): Call<ResultCoord2Address>
}