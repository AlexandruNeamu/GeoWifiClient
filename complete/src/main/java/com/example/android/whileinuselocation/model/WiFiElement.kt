package com.example.android.whileinuselocation.model

data class WiFiElement(
    val linkSpeed: Int,
    val frequency:Int,
    val RSSI: Int,
    val SSID:String,
    val BSSID:String
    )
