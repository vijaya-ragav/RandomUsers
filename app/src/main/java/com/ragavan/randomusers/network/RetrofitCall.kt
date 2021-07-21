package com.ragavan.randomusers.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitCall {

    //this function is tied to this class rather than to instances of it
    companion object {
        private const val BASE_URL = "https://randomuser.me/"
        const val WEATHER_URL = "http://api.weatherapi.com/v1/current.json"

        private val interceptor : HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
            this.level = HttpLoggingInterceptor.Level.BODY
        }

        private val client : OkHttpClient = OkHttpClient.Builder().apply {
            this.addInterceptor(interceptor)
        }.build()

        //lazy initialization is used to initialize retrofit instance once and only when it is used
        //by using lazy it is thread safe
        val retrofitClient: ApiRequest by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build().create(ApiRequest::class.java)
        }
    }



}