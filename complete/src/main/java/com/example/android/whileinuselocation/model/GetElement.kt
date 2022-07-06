package com.example.android.whileinuselocation.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "Results_table")
data class GetElement(
    @PrimaryKey(autoGenerate = true) val id:Int,
    val latitude:Double,
    val longitude:Double,
    val state:String,
    val SSID:String,
    val time:Double
)
