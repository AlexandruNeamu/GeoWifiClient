package com.example.android.whileinuselocation

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.android.whileinuselocation.data.ResultsRepository

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.example.android.whileinuselocation.databinding.ActivityMapsBinding
import com.example.android.whileinuselocation.model.GetElement
import com.google.android.gms.maps.model.*
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject


@AndroidEntryPoint
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    val points = mutableListOf<GetElement>()

    @Inject
    lateinit var repository: ResultsRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType=GoogleMap.MAP_TYPE_SATELLITE
        CoroutineScope(Dispatchers.IO).launch{
                val result = repository.getResults()
            CoroutineScope(Dispatchers.Main).launch {
                for (i in result) {
                    points.add(i)
                }
                println(points)
                val latLngs=mutableListOf<LatLng>()
                for(i in points)
                {
                    val points = LatLng(i.latitude,i.longitude)
                    latLngs.add(points)
                }
                val colors= intArrayOf(
                    Color.rgb(102,255,0),
                    Color.rgb(255,0,0)
                )
                val startPoints= floatArrayOf(0.2f,1f)
                val gradient=Gradient(colors,startPoints)
                val provider= HeatmapTileProvider.Builder()
                    .data(latLngs)
                    .gradient(gradient)
                    .opacity(0.7)
                    .radius(40)
                    .build()
                val overlay=mMap.addTileOverlay(TileOverlayOptions().tileProvider(provider))
            }

        }
        val latLng=LatLng(44.418275580336584, 26.086292695462866)
        val zoomLevel=17.0f
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoomLevel))
    }
}