package com.example.android.whileinuselocation.RetrofitHeper

import android.content.Context
import android.widget.Toast
import com.example.android.whileinuselocation.model.PostElement
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RestApiService {
    private lateinit var context: Context

    fun addWifiData(wifiData: PostElement, onResult: (Boolean?) -> Unit){
        val retrofit = RetrofitHelper.buildService(QuotesApi::class.java)
        retrofit.sendWifiData(wifiData).enqueue(
            object : Callback<PostElement> {
                override fun onFailure(call: Call<PostElement>, t: Throwable) {
                    println(t)
                    onResult(false)
                }
                override fun onResponse(call: Call<PostElement>, response: Response<PostElement>) {
                    //val addedWifiData = response.body()
                    val res=response.isSuccessful
                    println(res)
                    Toast.makeText(context, "Am primit cu succes de la server", Toast.LENGTH_LONG).show()
                    onResult(res)
                }
            }
        )
    }
    fun setContext(con: Context) {
        context=con
    }

}