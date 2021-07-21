package com.ragavan.randomusers.network

import com.ragavan.randomusers.model.RandomUserModel
import com.ragavan.randomusers.model.WeatherReportModel
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface ApiRequest {

    @GET("api")
    suspend fun getRandomUsers(@Query("page") page: Int,@Query("results") result: Int,@Query("seed") seed: String): RandomUserModel

    @GET
    suspend fun getWeather(@Url url:String, @Query("key") key: String, @Query("q") q: String, @Query("appid") aqi: String): WeatherReportModel

}