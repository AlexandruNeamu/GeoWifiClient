package com.example.android.whileinuselocation.RetrofitHeper

import com.example.android.whileinuselocation.model.GetElement
import com.example.android.whileinuselocation.model.PostElement
import kotlinx.coroutines.flow.Flow
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface QuotesApi {

    @Headers("Content-Type: application/json")
    @POST("/wifiData/")
    fun sendWifiData(@Body wifiElement: PostElement): Call<PostElement>

    @Headers("Content-Type: application/json")
    @GET("/status/")
     fun reciveData():Call<List<GetElement>>

     @Headers("Content-Type: application/json")
     @GET("/")
     fun checkConnection():Call<String>
}