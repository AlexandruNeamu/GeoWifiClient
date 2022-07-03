package com.example.android.whileinuselocation.RetrofitHeper

import com.example.android.whileinuselocation.model.PostElement
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface QuotesApi {

    @Headers("Content-Type: application/json")
    @POST("/wifiData/")
    fun sendWifiData(@Body wifiElement: PostElement): Call<PostElement>
}