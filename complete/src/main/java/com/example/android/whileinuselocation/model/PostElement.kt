package com.example.android.whileinuselocation.model

import com.google.gson.annotations.SerializedName

data class PostElement (
    @SerializedName("linkSpeed") val linkSpeed: Int,
    @SerializedName("frequency") val frequency:Int,
    @SerializedName("RSSI") val RSSI: Int,
    @SerializedName("SSID") val SSID:String,
    @SerializedName("BSSID")  val BSSID:String,
    @SerializedName("latitude")  val latitude:Double,
    @SerializedName("longitude") val longitude:Double
)