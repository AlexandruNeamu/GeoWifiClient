package com.example.android.whileinuselocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.format.Formatter
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.android.whileinuselocation.RetrofitHeper.RestApiService
import com.example.android.whileinuselocation.data.ResultsRepository
import com.example.android.whileinuselocation.model.LocationElement
import com.example.android.whileinuselocation.model.PostElement
import com.example.android.whileinuselocation.model.WiFiElement
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import javax.inject.Inject

class GetDatasActivity : AppCompatActivity() {
    lateinit var updateButton: Button
    lateinit var sendButton: Button
    lateinit var handler: Handler
    lateinit var elemToSend:PostElement
    @Inject
    lateinit var repositoryResults: ResultsRepository


    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var lastLocation: Location? = null
    private var latitudeLabel: String? = null
    private var longitudeLabel: String? = null
    private var latitudeText: TextView? = null
    private var longitudeText: TextView? = null

    companion object {
        private val TAG = "LocationProvider"
        private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_datas)
        latitudeLabel = resources.getString(R.string.latitudeBabel)
        longitudeLabel = resources.getString(R.string.longitudeBabel)
        latitudeText = findViewById<View>(R.id.latitudeText) as TextView
        longitudeText = findViewById<View>(R.id.longitudeText) as TextView
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        // Method to get the current connection info
        val wInfo = wifiManager.connectionInfo
        updateButton = findViewById<Button>(R.id.buttonGetWifiInfo)
        sendButton=findViewById<Button>(R.id.buttonSendWifiInfo)
        handler= Handler(Looper.getMainLooper())
        updateButton.setOnClickListener(listener)
        sendButton.setOnClickListener(listener)
    }
    public override fun onStart() {
        super.onStart()
        if (!checkPermissions()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions()
            }
        }
        else {
            getLastLocation()
        }
    }
    @SuppressLint("SetTextI18n")
    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient?.lastLocation!!.addOnCompleteListener(this) { task ->
            Log.w(TAG,task.toString())

            if (task.isSuccessful && task.result != null) {
                lastLocation = task.result
                latitudeText!!.text = latitudeLabel + ": " + (lastLocation)!!.latitude
                longitudeText!!.text = longitudeLabel + ": " + (lastLocation)!!.longitude
                Log.w(TAG, lastLocation!!.latitude.toString(), task.exception)
            }
            else {
                Log.w(TAG, "getLastLocation:exception", task.exception)
                showMessage("No location detected. Make sure location is enabled on the device.")
            }
        }
    }
    private fun returnLastLocation(): LocationElement
    {
        val location=LocationElement(lastLocation!!.latitude,lastLocation!!.longitude)
        return location
    }

    private fun showMessage(string: String) {
        val container = findViewById<View>(R.id.linearLayout)
        if (container != null) {
            Toast.makeText(this@GetDatasActivity, string, Toast.LENGTH_LONG).show()
        }
    }
    private fun showSnackbar(
        mainTextStringId: String, actionStringId: String,
        listener: View.OnClickListener
    ) {
        Toast.makeText(this@GetDatasActivity, mainTextStringId, Toast.LENGTH_LONG).show()
    }
    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return permissionState == PackageManager.PERMISSION_GRANTED
    }
    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
            this@GetDatasActivity,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }
    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION

        )
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            showSnackbar("Location permission is needed for core functionality", "Okay",
                View.OnClickListener {
                    startLocationPermissionRequest()
                })
        }
        else {
            Log.i(TAG, "Requesting permission")
            startLocationPermissionRequest()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> {
                    // If user interaction was interrupted, the permission request is cancelled and you
                    // receive empty arrays.
                    Log.i(TAG, "User interaction was cancelled.")
                }
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    // Permission granted.
                    getLastLocation()
                }
                else -> {
                    showSnackbar("Permission was denied", "Settings",
                        View.OnClickListener {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                Build.DISPLAY, null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }

    val listener= View.OnClickListener {
            view ->
        when (view.id){
            R.id.buttonGetWifiInfo-> {
                val elemWifi = rewind()
                getLastLocation()
                val elemLocation = returnLastLocation()
                elemToSend = PostElement(
                    elemWifi.linkSpeed, elemWifi.frequency,
                    elemWifi.RSSI, elemWifi.SSID, elemWifi.BSSID,
                    elemLocation.latitude, elemLocation.longitude
                )
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {

                }
            }
            R.id.buttonSendWifiInfo->
            {

                val apiService= RestApiService(repositoryResults,applicationContext)
                apiService.setContext(this@GetDatasActivity)
                apiService.addWifiData(elemToSend){
                    if(it==true)
                    {
                        Toast.makeText(this@GetDatasActivity, "Am primit cu succes de la server", Toast.LENGTH_LONG).show()
                        //println("AM PRIMIT DE LA SERVER")
                    }
                    else
                    {
                        Toast.makeText(this@GetDatasActivity,"Eroare", Toast.LENGTH_LONG).show()
                    }
                }

            }
        }
    }
    @SuppressLint("SetTextI18n")
    fun rewind():WiFiElement
    {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wInfo = wifiManager.connectionInfo
        val ipAddress = Formatter.formatIpAddress(wInfo.ipAddress)
        val linkSpeed = wInfo.linkSpeed
        val networkID = wInfo.networkId
        val ssid = wInfo.ssid
        val hssid = wInfo.hiddenSSID
        val bssid = wInfo.bssid
        val rssi = wInfo.rssi
        val freq = wInfo.frequency


        val toSend= WiFiElement(linkSpeed,freq,rssi,ssid,bssid)

        // Finding the textView from the layout file
        val wifiInformationTv = findViewById<TextView>(R.id.textWifiInfo)

        // Setting the text inside the textView with
        // various entities of the connection
        wifiInformationTv.text =

            "IP Address:\t$ipAddress\n" +
                    "Link Speed:\t$linkSpeed\n" +
                    "Network ID:\t$networkID\n" +
                    "SSID:\t$ssid\n" +
                    "Hidden SSID:\t$hssid\n" +
                    "BSSID:\t$bssid\n" +
                    "Frequence:\t$freq\n" +
                    "RSSI:\t$rssi\n"


        return toSend
    }

}