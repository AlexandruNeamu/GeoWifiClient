package com.example.android.whileinuselocation.RetrofitHeper

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.android.whileinuselocation.data.ResultsRepository
import com.example.android.whileinuselocation.model.GetElement
import com.example.android.whileinuselocation.model.PostElement
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class RestApiService @Inject constructor(
    private val repo:ResultsRepository,
    private val con: Context
){
    private lateinit var context: Context

    fun getData(onResult: (Boolean?) -> Unit){
        val retrofit=RetrofitHelper.buildService(QuotesApi::class.java)
        retrofit.reciveData().enqueue(
            object :Callback<List<GetElement>>{
                override fun onFailure(call: Call<List<GetElement>>, t: Throwable) {
                    onResult(false)
                    println(t)
                }

                override fun onResponse(
                    call: Call<List<GetElement>>,
                    response: Response<List<GetElement>>
                ) {
                    val resp=response.body()
                    //println(resp)
//                    repo.DeleteResults()
                    for(i in resp!!)
                    {
                        repo.updateResults(i)
                        println(i)
                    }
                    onResult(true)
                }
            }
        )
    }

    fun check(onResult: (Boolean?) -> Unit){
        val retrofit=RetrofitHelper.buildService(QuotesApi::class.java)
        retrofit.checkConnection().enqueue(
            object:Callback<String>
            {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    onResult(true)
                }
                override fun onFailure(call: Call<String>, t: Throwable) {
                    onResult(false)
                }
            }
        )
    }

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
                    //println(res)
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