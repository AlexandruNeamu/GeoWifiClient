/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.whileinuselocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.SupplicantState
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.text.format.Formatter
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.*
import com.example.android.whileinuselocation.RetrofitHeper.RestApiService
import com.example.android.whileinuselocation.data.LocationRepository
import com.example.android.whileinuselocation.data.ResultsRepository
import com.example.android.whileinuselocation.model.LocationElement
import com.example.android.whileinuselocation.model.PostElement
import com.example.android.whileinuselocation.model.WiFiElement
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


private const val TAG = "LocationActivity"
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34


@AndroidEntryPoint
class LocationActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private var foregroundOnlyLocationServiceBound = false

    // Provides location updates for while-in-use feature.
    private var foregroundOnlyLocationService: ForegroundOnlyLocationService? = null

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var foregroundOnlyLocationButton: Button

    private lateinit var outputTextView: TextView
    @Inject
    lateinit var repositoryResult:ResultsRepository

    // Monitors connection to the while-in-use service.
    private val foregroundOnlyServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as ForegroundOnlyLocationService.LocalBinder
            foregroundOnlyLocationService = binder.service
            foregroundOnlyLocationServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            foregroundOnlyLocationService = null
            foregroundOnlyLocationServiceBound = false
        }
    }

    // Data store (in this case, Room database) from which to receive location updates via Flow, injected via Hilt
    @Inject
    lateinit var repository: LocationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_location)

        sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        foregroundOnlyLocationButton = findViewById(R.id.foreground_only_location_button)
        outputTextView = findViewById(R.id.output_text_view)
        outputTextView.movementMethod = ScrollingMovementMethod()

        foregroundOnlyLocationButton.setOnClickListener {
            val enabled = sharedPreferences.getBoolean(
                SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false
            )

            if (enabled) {
                foregroundOnlyLocationService?.unsubscribeToLocationUpdates()
            } else {
                // TODO: Step 1.0, Review Permissions: Checks and requests if needed.
                if (foregroundPermissionApproved()) {
                    foregroundOnlyLocationService?.subscribeToLocationUpdates()
                        ?: Log.d(TAG, "Service Not Bound")
                } else {
                    requestForegroundPermissions()
                }
            }
        }

        // Observe locations via Flow as they are inserted into Room by the Service
        repository.getLocations()
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach {
                if (it.isNotEmpty()) {
                    logResultsToScreen("Loca??ie actual??: ${it[it.size - 1].toText()}")
                    val wifiElement=rewind()
                    val location=it[it.size-1]
                    val latitude=location.latitude
                    val longitude=location.longitude
                    val locationElement=LocationElement(latitude,longitude)
                    val postElement=PostElement(wifiElement.linkSpeed,wifiElement.frequency
                        ,wifiElement.RSSI,wifiElement.SSID,wifiElement.BSSID,
                        locationElement.latitude,locationElement.longitude)
                    val apiService= RestApiService(repositoryResult,applicationContext)
                    apiService.setContext(this@LocationActivity)


                    apiService.addWifiData(postElement){
                        if(it==true)
                        {
                            Toast.makeText(this@LocationActivity, "Am primit cu succes de la server", Toast.LENGTH_LONG).show()
                            //println("AM PRIMIT DE LA SERVER")
                        }
                        else
                        {
                            Toast.makeText(this@LocationActivity,"Serverul este offline", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            .launchIn(lifecycleScope)
    }

    override fun onStart() {
        super.onStart()

        updateButtonState(
            sharedPreferences.getBoolean(SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)
        )
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        val serviceIntent = Intent(this, ForegroundOnlyLocationService::class.java)
        bindService(serviceIntent, foregroundOnlyServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        if (foregroundOnlyLocationServiceBound) {
            unbindService(foregroundOnlyServiceConnection)
            foregroundOnlyLocationServiceBound = false
        }
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)

        super.onStop()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        // Updates button states if new while in use location is added to SharedPreferences.
        if (key == SharedPreferenceUtil.KEY_FOREGROUND_ENABLED) {
            updateButtonState(
                sharedPreferences.getBoolean(
                    SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false
                )
            )
        }
    }

    // TODO: Step 1.0, Review Permissions: Method checks if permissions approved.
    private fun foregroundPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    // TODO: Step 1.0, Review Permissions: Method requests permissions.
    private fun requestForegroundPermissions() {
        val provideRationale = foregroundPermissionApproved()

        // If the user denied a previous request, but didn't check "Don't ask again", provide
        // additional rationale.
        if (provideRationale) {
            Snackbar.make(
                findViewById(R.id.activity_main),
                R.string.permission_rationale,
                Snackbar.LENGTH_LONG
            )
                .setAction(R.string.ok) {
                    // Request permission
                    ActivityCompat.requestPermissions(
                        this@LocationActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
                    )
                }
                .show()
        } else {
            Log.d(TAG, "Request foreground only permission")
            ActivityCompat.requestPermissions(
                this@LocationActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    // TODO: Step 1.0, Review Permissions: Handles permission result.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionResult")

        when (requestCode) {
            REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE -> when {
                grantResults.isEmpty() ->
                    // If user interaction was interrupted, the permission request
                    // is cancelled and you receive empty arrays.
                    Log.d(TAG, "User interaction was cancelled.")
                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    // Permission was granted.
                    foregroundOnlyLocationService?.subscribeToLocationUpdates()
                else -> {
                    // Permission denied.
                    updateButtonState(false)

                    Snackbar.make(
                        findViewById(R.id.activity_main),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(R.string.settings) {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                BuildConfig.APPLICATION_ID,
                                null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .show()
                }
            }
        }
    }

    private fun updateButtonState(trackingLocation: Boolean) {
        if (trackingLocation) {
            foregroundOnlyLocationButton.text = getString(R.string.stop_location_updates_button_text)
        } else {
            foregroundOnlyLocationButton.text = getString(R.string.start_location_updates_button_text)
        }
    }

    private fun logResultsToScreen(output: String) {
        val outputWithPreviousLogs = "$output\n${outputTextView.text}"
        outputTextView.text = outputWithPreviousLogs

    }
    @SuppressLint("SetTextI18n")
    fun rewind(): WiFiElement
    {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wInfo = wifiManager.connectionInfo
        println(wInfo)
        var linkSpeed = 0
        var ssid = "WiFiStudenti"
        var bssid = "02:00:00:00:00:00"
        var rssi = -127
        var freq = -1
        val supplicantState=wInfo.supplicantState
        if(!supplicantState.equals(SupplicantState.COMPLETED))
        {
            linkSpeed = 0
            ssid = "WiFiStudenti"
            bssid = "02:00:00:00:00:00"
            rssi = -127
            freq = -1
        }
        //val ipAddress = Formatter.formatIpAddress(wInfo.ipAddress)
        else
        {
            linkSpeed = wInfo.linkSpeed
            ssid = wInfo.ssid
            bssid = wInfo.bssid
            rssi = wInfo.rssi
            freq = wInfo.frequency
        }


        val toSend= WiFiElement(linkSpeed,freq,rssi,ssid,bssid)
        return toSend
    }
}
