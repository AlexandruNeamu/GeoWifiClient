package com.example.android.whileinuselocation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.android.whileinuselocation.RetrofitHeper.RestApiService
import com.example.android.whileinuselocation.data.ResultsRepository
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var repositoryResult: ResultsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val apiService= RestApiService(repositoryResult,applicationContext)
        apiService.setContext(this@MainActivity)
        apiService.getData {
            if(it==true)
            {
                println(it)
                println("MERGEEEEEE")
            }
            else
            {
                println("NU MERGEEEEE")
            }
        }



//        val fragment: Fragment = MapsFragment()
//
//          Open fragment
//        val mapFragment = supportFragmentManager
//            .findFragmentById(R.id.map) as SupportMapFragment?
//        mapFragment!!.getMapAsync(this)
    }

}