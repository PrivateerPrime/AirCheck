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
import android.content.pm.PackageManager
import android.location.LocationListener
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.android.volley.Request

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var requestQueue: RequestQueue
    private lateinit var pollutionData: PollutionDataClass
    private lateinit var metricsData: MetricsDataClass
    private lateinit var preferences: SharedPreferences
    private lateinit var locationManager: LocationManager

    /*
       TODO GPS nie działa prawidłowo na emulatorze, testować tylko na FIZYCZNEJ maszynie
       TODO Zmiana others na inne wskaźniki (podobna lista elementów z innymi parametrami, c0, nh3, so2;
       TODO mogą to być inne dane atmosferyczne
       TODO Poprawa settings
    */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        Log.i("t", "klikRefresh")
        getData()
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        preferences = getPreferences(Context.MODE_PRIVATE)
        if (preferences.getString("Lan", "en") == "pl")
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

        requestQueue = RequestQueue(DiskBasedCache(cacheDir, 1024*1024), BasicNetwork((HurlStack()))).apply {
            start()
        }
    }

    private fun setPollutionData() {
        val editor = preferences.edit()
        for (iter in 0..24) {
            val tagString = "pollutionMain$iter"
            val pollutionValue = pollutionData.list[iter].components.pm10
            editor.putString(tagString, "$pollutionValue µg/m³")
            editor.apply()
        }

        val hour = preferences.getFloat("forecastRange", 0F).toInt()
        val pollution = preferences.getString("pollutionMain$hour", "NODATA")

        findViewById<TextView>(R.id.text_pollution).text = pollution

    }

    private fun setMetricsData() {
        val editor = preferences.edit()
        val temperatureValue = metricsData.current.temp
        val humidityValue = metricsData.current.humidity
        val pressureValue = metricsData.current.pressure
        editor.putString("temperatureMain0", "$temperatureValue")
        editor.putString("humidityMain0", "$humidityValue%")
        editor.putString("pressureMain0", "$pressureValue hPa")
        editor.apply()

        for (iter in 0..23) {
            val temperatureString = "temperatureMain${iter+1}"
            val humidityString = "humidityMain${iter+1}"
            val pressureString = "pressureMain${iter+1}"
            val temperatureValueForecast = metricsData.hourly[iter].temp
            val humidityValueForecast = metricsData.hourly[iter].humidity
            val pressureValueForecast = metricsData.hourly[iter].pressure
            editor.putString(temperatureString, "$temperatureValueForecast")
            editor.putString(humidityString, "$humidityValueForecast%")
            editor.putString(pressureString, "$pressureValueForecast hPa")
            editor.apply()
        }

        val hour = preferences.getFloat("forecastRange", 0F).toInt()
        var temperature = preferences.getString("temperatureMain$hour", "0.0")?.toFloat()
        if (preferences.getString("Temp", "Cel") == "Fah") {
            temperature = ((temperature!! * 9.0/5.0) + 32.0).toFloat()
        }
        val temperatureText = temperature.toString() + if (preferences.getString("Temp", "Cel") == "Cel")
            getString(R.string.res_celsius)
        else
            getString(R.string.res_fahrenheit)

        val humidity = preferences.getString("humidityMain$hour", "NODATA")
        val pressure = preferences.getString("pressureMain$hour", "NODATA")

        findViewById<TextView>(R.id.text_temperature).text = temperatureText
        findViewById<TextView>(R.id.text_humidity).text = humidity
        findViewById<TextView>(R.id.text_pressure).text = pressure

    }

    @SuppressLint("SetTextI18n")
    private fun getTime() {
        val cal = Calendar.getInstance()
        val editor = preferences.edit()
        var hour = cal.get(Calendar.HOUR_OF_DAY).toString()
        var minute = cal.get(Calendar.MINUTE).toString()
        if (hour.toInt() < 10)
            hour = "0$hour"
        if (minute.toInt() < 10)
            minute = "0$minute"
        val day: Int = cal.get(Calendar.DAY_OF_WEEK)
        lateinit var dayName: String
        when (cal.get(Calendar.DAY_OF_WEEK)) {
            1 -> dayName = getString(R.string.res_Sunday)
            2 -> dayName = getString(R.string.res_Monday)
            3 -> dayName = getString(R.string.res_Tuesday)
            4 -> dayName = getString(R.string.res_Wednesday)
            5 -> dayName = getString(R.string.res_Thursday)
            6 -> dayName = getString(R.string.res_Friday)
            7 -> dayName = getString(R.string.res_Saturday)
        }

        editor.putString("time0", "$hour:$minute")
        editor.putInt("day0", day)
        editor.apply()
        for (i in 1..24) {
            var hourForecast = ((cal.get(Calendar.HOUR_OF_DAY) + i) % 24).toString()
            if (hourForecast.toInt() < 10)
                hourForecast = "0$hourForecast"
            var dayForecast = cal.get(Calendar.DAY_OF_WEEK)
            if ((cal.get(Calendar.HOUR_OF_DAY) + i) >= 24) {
                dayForecast = (dayForecast + 1) % 7
            }
            editor.putString("time${i}", "$hourForecast:00")
            editor.putInt("day${i}", dayForecast)
            editor.apply()
        }
        val hourSlider = preferences.getFloat("forecastRange", 0F).toInt()
        val timeString = preferences.getString("time$hourSlider", "NODATA")
        if ((cal.get(Calendar.HOUR_OF_DAY) + hourSlider) >= 24) {
            val dayForecast = preferences.getInt("day${hourSlider}", 0)
            lateinit var dayForecastName: String
            when (dayForecast) {
                0 -> dayForecastName = getString(R.string.res_Saturday)
                1 -> dayForecastName = getString(R.string.res_Sunday)
                2 -> dayForecastName = getString(R.string.res_Monday)
                3 -> dayForecastName = getString(R.string.res_Tuesday)
                4 -> dayForecastName = getString(R.string.res_Wednesday)
                5 -> dayForecastName = getString(R.string.res_Thursday)
                6 -> dayForecastName = getString(R.string.res_Friday)
                7 -> dayForecastName = getString(R.string.res_Saturday)
            }
            findViewById<TextView>(R.id.text_time).text = "$dayForecastName, $timeString"
        }
        else
            findViewById<TextView>(R.id.text_time).text = "$dayName, $timeString"
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
        var location: Location? = null
        for (i in providers.indices.reversed()) {
            location = locationManager.getLastKnownLocation(providers[i])
            if (location != null) break
        }
        val gps = DoubleArray(2)
        if (location != null) {
            Log.i("lat", location.latitude.toString()); Log.i("lng", location.longitude.toString())
            gps[0] = location.latitude
            gps[1] = location.longitude

            val maxDistanceKM: String = if (preferences.getString("Unit", "Km") == "Km")
                preferences.getFloat("searchRange", 5.0F).toString()
            else
                (preferences.getFloat("searchRange", 5.0F)*1.609344).toString()
            val pollutionUrl = "https://api.openweathermap.org/data/2.5/air_pollution/forecast?lat=${location.latitude}&lon=${location.longitude}&appid=0320e3284b492358bcc9752cd5796e03"
            val pollutionRequest = StringRequest(Request.Method.GET, pollutionUrl, {
                    response -> Log.i("resp", response)
                    pollutionData = Gson().fromJson(response, PollutionDataClass::class.java)
                    setPollutionData()
            }, {
                    error -> Log.e("resp", error.toString())
            })
            val metricsUrl = "https://api.openweathermap.org/data/2.5/onecall?lat=${location.latitude}&lon=${location.longitude}&exclude=minutely,daily,alerts&units=metric&appid=0320e3284b492358bcc9752cd5796e03"
            val metricsRequest = StringRequest(Request.Method.GET, metricsUrl, {
                    response -> Log.i("resp", response)
                    metricsData = Gson().fromJson(response, MetricsDataClass::class.java)
                    setMetricsData()
            }, {
                    error -> Log.e("resp", error.toString())
            })

            try {
                requestQueue.add(pollutionRequest)
                requestQueue.add(metricsRequest)
                getTime()
                Toast.makeText(applicationContext, getString(R.string.res_sync_correct), Toast.LENGTH_SHORT).show()
            }
            catch (e: Exception) {
                Log.e("err", e.printStackTrace().toString())
                Toast.makeText(applicationContext, getString(R.string.res_sync_error), Toast.LENGTH_SHORT).show()
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

    private fun setLocale() {
        val locale = Locale("pl")
        val res: Resources = resources
        val dm: DisplayMetrics = res.displayMetrics
        val conf: Configuration = res.configuration
        conf.setLocale(locale)
        Locale.setDefault(locale)
        conf.setLayoutDirection(locale)
        res.updateConfiguration(conf, dm)
    }
}