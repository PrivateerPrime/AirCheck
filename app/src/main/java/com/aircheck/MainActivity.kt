package com.aircheck

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.aircheck.databinding.ActivityMainBinding
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import java.lang.Exception
import java.util.*
import kotlin.collections.HashMap
import android.content.pm.PackageManager
import android.location.LocationListener
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var requestQueue: RequestQueue
    private lateinit var installationData: InstallationData
    private lateinit var preferences: SharedPreferences
    private lateinit var locationManager: LocationManager

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_app_bar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /*
       TODO Dodać jeszcze jedno zapytanie API zwracające temperaturę, wilgotność, ciśnienie na następne 24 godziny
       TODO GPS nie działa prawidłowo na emulatorze, testować tylko na FIZYCZNEJ maszynie
       TODO Dodać tekst pokazujący godzinę/datę/czas od ostatniej synhcronizacji
    */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        Log.i("t", "klikRefresh")
        getData()
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        preferences = getPreferences(Context.MODE_PRIVATE)
        if (getPreferences(Context.MODE_PRIVATE).getString("Lan", "en") == "pl")
            setLocale()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home, R.id.navigation_settings, R.id.navigation_other))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // sprawdzenie uprawnień lokalizacyjnych
        checkForPermissions()

        // kolejka wysyłań API

        val cache = DiskBasedCache(cacheDir, 1024*1024)
        val network = BasicNetwork((HurlStack()))

        requestQueue = RequestQueue(cache, network).apply {
            start()
        }
    }

    private fun setLocale() {
        val locale = Locale("pl")
        val res: Resources = resources
        val dm: DisplayMetrics = res.displayMetrics
        val conf: Configuration = res.configuration
        conf.locale = locale
        Locale.setDefault(locale)
        conf.setLayoutDirection(locale)
        res.updateConfiguration(conf, dm)
    }

    private fun checkForPermissions() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) &&
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {

                AlertDialog.Builder(this)
                    .setTitle(R.string.res_location_needed)
                    .setMessage(R.string.res_location_message)
                    .setPositiveButton(
                        R.string.res_accept
                    ) { _, _ ->
                        ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),99)
                    }
                    .create()
                    .show()
            }
            else {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),99)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setMainData() {
        val indexPollution = installationData.current.values.find { it.name == "PM10" }?.value
        val indexTemperature = installationData.current.values.find { it.name == "TEMPERATURE" }?.value
        val indexHumidity = installationData.current.values.find { it.name == "HUMIDITY" }?.value
        val indexPressure = installationData.current.values.find { it.name == "PRESSURE" }?.value
        val forecastList = installationData.forecast.flatMap { it.values }.filter { it.name == "PM10" }
        val editor = preferences.edit()
        editor.putString("pollutionMain0", indexPollution.toString() + " µg/m³")
        editor.putString("temperatureMain0", indexTemperature.toString() + " °C")
        editor.putString("humidityMain0", indexHumidity.toString() + "%")
        editor.putString("pressureMain0", indexPressure.toString() + " hPa")
        editor.apply()
        var it = 1
        for (item in forecastList) {
            val tagString = "pollutionMain$it"
            editor.putString(tagString, item.value.toString() + " µg/m³")
            editor.apply()
            it++
        }

        val hour = preferences.getFloat("forecastRange", 0F).toInt()
        val tempPollution = preferences.getString("pollutionMain$hour", "NODATA")
        val tempTemperature = preferences.getString("temperatureMain$hour", "NODATA")
        val tempHumidity = preferences.getString("humidityMain$hour", "NODATA")
        val tempPressure = preferences.getString("pressureMain$hour", "NODATA")

        findViewById<TextView>(R.id.text_pollution).text = tempPollution
        findViewById<TextView>(R.id.text_temperature).text = tempTemperature
        findViewById<TextView>(R.id.text_humidity).text = tempHumidity
        findViewById<TextView>(R.id.text_pressure).text = tempPressure
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {Log.i("loc", location.latitude.toString() + " " + location.longitude)}
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    @SuppressLint("MissingPermission")
    private fun getData() {
        val providers = locationManager.allProviders
        var l: Location? = null
        for (i in providers.indices.reversed()) {
            l = locationManager.getLastKnownLocation(providers[i])
            if (l != null) break
        }
        val gps = DoubleArray(2)
        if (l != null) {
            Log.i("lat", l.latitude.toString()); Log.i("lng", l.longitude.toString())
            gps[0] = l.latitude
            gps[1] = l.longitude

            val maxDistanceKM: String = if (preferences.getString("Unit", "Km") == "Km")
                preferences.getFloat("searchRange", 5.0F).toString()
            else
                (preferences.getFloat("searchRange", 5.0F)*1.609344).toString()
            val url = "https://airapi.airly.eu/v2/measurements/nearest?lat=${l.latitude}&lng=${l.longitude}&maxDistanceKM=${maxDistanceKM}"
            val stringRequest = object: StringRequest(Method.GET, url, {
                    response -> Log.i("resp", response)
                                installationData = Gson().fromJson(response, InstallationData::class.java)
                                setMainData()
            }, {
                    error -> Log.e("resp", error.toString())
            }
            )
            {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["apikey"] = "hILP4tnLsNjzv3Y1QHu1nG3TH1ehLpQ5"
                    return headers
                }
            }
            try {
                requestQueue.add(stringRequest)
            }
            catch (e: Exception) {
                Log.e("err", e.printStackTrace().toString())
            }
        }
        else {
            AlertDialog.Builder(this)
                .setTitle(R.string.res_no_location)
                .setMessage(R.string.res_no_location_message)
                .setPositiveButton(
                    R.string.res_accept
                ) { _, _ ->

                }
                .create()
                .show()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.i("t","per")

        when (requestCode) {
            99 -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        0,
                        10F,
                        locationListener
                    )
                    getData()
                }
                else {
                    AlertDialog.Builder(this)
                        .setTitle(R.string.res_location_no_permissions)
                        .setMessage(R.string.res_location_no_permissions_message)
                        .setPositiveButton(
                            R.string.res_accept
                        ) { _, _ ->

                        }
                        .create()
                        .show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}