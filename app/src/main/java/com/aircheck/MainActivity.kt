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
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.aircheck.databinding.ActivityMainBinding
import com.android.volley.RequestQueue
import com.google.gson.Gson
import java.lang.Exception
import java.util.*
import android.content.pm.PackageManager
import android.location.LocationListener
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.aircheck.apidata.MetricsDataClass
import com.aircheck.apidata.PollutionDataClass
import com.android.volley.Request
import com.android.volley.toolbox.*
import java.math.RoundingMode

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var requestQueue: RequestQueue
    private lateinit var pollutionData: PollutionDataClass
    private lateinit var metricsData: MetricsDataClass
    private lateinit var preferences: SharedPreferences
    private lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        preferences = getPreferences(Context.MODE_PRIVATE)
        if (preferences.getString("Lan", "en") == "pl")
            setLocale("pl")
        else
            setLocale("en")
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

    @SuppressLint("SetTextI18n")
    fun getDataHome() {
        val (location, securityError) = getLocation()
        if (location != null) {
            Log.i("lat", location.latitude.toString()); Log.i("lng", location.longitude.toString())
            val pollutionUrl = "https://api.openweathermap.org/data/2.5/air_pollution/forecast?lat=${location.latitude}&lon=${location.longitude}&appid=0320e3284b492358bcc9752cd5796e03"
            val pollutionRequest = StringRequest(Request.Method.GET, pollutionUrl, {

                    response -> Log.i("resp", response)
                pollutionData = Gson().fromJson(response, PollutionDataClass::class.java)
                getPollutionData()

                val hour = preferences.getFloat("forecastHomeRange", 0F).toInt()
                val pollution = preferences.getString("pollutionMain$hour", "NODATA")
                findViewById<TextView>(R.id.text_pollution).text = pollution

            }, {
                    error -> Log.e("resp", error.toString())
                    AlertDialog.Builder(this)
                        .setTitle(R.string.res_no_internet_connection_title)
                        .setMessage(R.string.res_no_internet_connection_message)
                        .setPositiveButton(
                            R.string.res_accept
                        ) { _, _ ->

                        }
                        .create()
                        .show()
            })
            val metricsUrl = "https://api.openweathermap.org/data/2.5/onecall?lat=${location.latitude}&lon=${location.longitude}&exclude=minutely,daily,alerts&units=metric&appid=0320e3284b492358bcc9752cd5796e03"
            val metricsRequest = StringRequest(Request.Method.GET, metricsUrl, {

                    response -> Log.i("resp", response)
                metricsData = Gson().fromJson(response, MetricsDataClass::class.java)
                getMetricsData()
                getOthersData()

                val hour = preferences.getFloat("forecastHomeRange", 0F).toInt()
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

                Toast.makeText(applicationContext, getString(R.string.res_sync_correct), Toast.LENGTH_SHORT).show()
            }, {
                    error -> Log.e("resp", error.toString())
                    AlertDialog.Builder(this)
                        .setTitle(R.string.res_no_internet_connection_title)
                        .setMessage(R.string.res_no_internet_connection_message)
                        .setPositiveButton(
                            R.string.res_accept
                        ) { _, _ ->

                        }
                        .create()
                        .show()
            })
            try {
                requestQueue.add(pollutionRequest)
                requestQueue.add(metricsRequest)
                val (dayName, timeString) = getTime(1)
                findViewById<TextView>(R.id.text_home_time).text = "$dayName, $timeString"
            }
            catch (e: Exception) {
                Log.e("err", e.printStackTrace().toString())
                Toast.makeText(applicationContext, getString(R.string.res_sync_error), Toast.LENGTH_SHORT).show()
            }
        }
        else {
            if (!securityError)
            {
                AlertDialog.Builder(this)
                    .setTitle(R.string.res_no_location_title)
                    .setMessage(R.string.res_no_location_message)
                    .setPositiveButton(
                        R.string.res_accept
                    ) { _, _ ->

                    }
                    .create()
                    .show()
            }
            else {
                AlertDialog.Builder(this)
                    .setTitle(R.string.res_location_no_permissions_title)
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

    @SuppressLint("SetTextI18n")
    fun getDataOthers() {
        val (location, securityError) = getLocation()
        if (location != null) {
            Log.i("lat", location.latitude.toString()); Log.i("lng", location.longitude.toString())

            val pollutionUrl = "https://api.openweathermap.org/data/2.5/air_pollution/forecast?lat=${location.latitude}&lon=${location.longitude}&appid=0320e3284b492358bcc9752cd5796e03"
            val pollutionRequest = StringRequest(Request.Method.GET, pollutionUrl, {

                    response -> Log.i("resp", response)
                pollutionData = Gson().fromJson(response, PollutionDataClass::class.java)
                getPollutionData()

            }, {
                    error -> Log.e("resp", error.toString())
                AlertDialog.Builder(this)
                    .setTitle(R.string.res_no_internet_connection_title)
                    .setMessage(R.string.res_no_internet_connection_message)
                    .setPositiveButton(
                        R.string.res_accept
                    ) { _, _ ->

                    }
                    .create()
                    .show()
            })


            val metricsUrl = "https://api.openweathermap.org/data/2.5/onecall?lat=${location.latitude}&lon=${location.longitude}&exclude=minutely,daily,alerts&units=metric&appid=0320e3284b492358bcc9752cd5796e03"
            val metricsRequest = StringRequest(Request.Method.GET, metricsUrl, {
                    response -> Log.i("resp", response)
                metricsData = Gson().fromJson(response, MetricsDataClass::class.java)
                getOthersData()
                getMetricsData()

                val hour = preferences.getFloat("forecastOtherRange", 0F).toInt()
                val textWind = findViewById<TextView>(R.id.text_wind)
                val textWeather = findViewById<TextView>(R.id.text_weather)
                val textUv = findViewById<TextView>(R.id.text_uv)
                val textVisibility = findViewById<TextView>(R.id.text_visibility)
                val imageWeather = findViewById<ImageView>(R.id.image_weather)


                if (preferences.getString("windSpeedOthers$hour", "NODATA") != "NODATA")
                {
                    if (preferences.getString("Unit", "Km") == "Km") {
                        textWind.text = "${preferences.getString("windSpeedOthers$hour", "NODATA")} m/s"
                        textVisibility.text = "${(preferences.getString("visibilityOthers$hour", "NODATA")!!.toDouble() / 1000).toBigDecimal().setScale(1,
                            RoundingMode.HALF_EVEN)} km"
                    }
                    else {
                        textWind.text = "${(preferences.getString("windSpeedOthers$hour", "NODATA")!!.toDouble() / 0.44704).toBigDecimal().setScale(2,
                            RoundingMode.HALF_EVEN)} mph"
                        textVisibility.text = "${(preferences.getString("visibilityOthers$hour", "NODATA")!!.toDouble() / 1000 / 1.60934).toBigDecimal().setScale(1, RoundingMode.HALF_EVEN)} mil"
                    }
                }
                else {
                    textWind.text = "NODATA"
                    textVisibility.text = "NODATA"
                }

                when (preferences.getString("weatherOthers$hour", "NODATA")) {
                    "Rain", "Drizzle" -> {
                        imageWeather.setImageResource(R.drawable.rain)
                        if (preferences.getString("precipitationOthers$hour", "NODATA") != "NODATA")
                            setWeather(textWeather, hour)
                        else
                            textWeather.text = "NODATA"
                    }
                    "Snow" -> {
                        imageWeather.setImageResource(R.drawable.snow)
                        if (preferences.getString("precipitationOthers$hour", "NODATA") != "NODATA")
                            setWeather(textWeather, hour)
                        else
                            textWeather.text = "NODATA"
                    }
                    "Thunderstorm" -> {
                        imageWeather.setImageResource(R.drawable.thunder)
                        if (preferences.getString("precipitationOthers$hour", "NODATA") != "NODATA") {
                            if (preferences.getString("precipitationOthers$hour", "NODATA") != "NoPrec")
                                setWeather(textWeather, hour)
                            else
                                textWeather.text = getString(R.string.res_no_precipitation)
                        }
                        else
                            textWeather.text = "NODATA"
                    }
                    "Atmosphere", "Clouds" -> {
                        imageWeather.setImageResource(R.drawable.ic_cloud_foreground)
                        textWeather.text = getString(R.string.res_no_precipitation)
                    }
                    "Clear" -> {
                        imageWeather.setImageResource(R.drawable.sunny)
                        textWeather.text = getString(R.string.res_no_precipitation)
                    }
                    else -> {
                        imageWeather.setImageResource(R.drawable.ic_cloud_foreground)
                        textWeather.text = "NODATA"
                    }
                }

                val uviValue = preferences.getString("uviOthers$hour", "NODATA")
                if (uviValue != "NODATA") {
                    val uviValueTemp = uviValue!!.toDouble()
                    when {
                        uviValueTemp < 3.0 -> textUv.text = uviValue.toString() + ": " + getString(R.string.res_uv_low)
                        uviValueTemp < 6.0 -> textUv.text = uviValue.toString() + ": " + getString(R.string.res_uv_moderate)
                        uviValueTemp < 8.0 -> textUv.text = uviValue.toString() + ": " + getString(R.string.res_uv_high)
                        uviValueTemp < 11.0 -> textUv.text = uviValue.toString() + ": " + getString(R.string.res_uv_very_high)
                        else -> textUv.text = uviValue.toString() + ": " + getString(R.string.res_uv_extreme)
                    }
                }
                else
                    textUv.text = "NODATA"

                Toast.makeText(applicationContext, getString(R.string.res_sync_correct), Toast.LENGTH_SHORT).show()
            }, {
                    error -> Log.e("resp", error.toString())
                AlertDialog.Builder(this)
                    .setTitle(R.string.res_no_internet_connection_title)
                    .setMessage(R.string.res_no_internet_connection_message)
                    .setPositiveButton(
                        R.string.res_accept
                    ) { _, _ ->}
                    .create()
                    .show()
            })

            try {
                requestQueue.add(metricsRequest)
                requestQueue.add(pollutionRequest)
                val (dayName, timeString) = getTime(2)
                findViewById<TextView>(R.id.text_other_time).text = "$dayName, $timeString"
            }
            catch (e: Exception) {
                Log.e("err", e.printStackTrace().toString())
                Toast.makeText(applicationContext, getString(R.string.res_sync_error), Toast.LENGTH_SHORT).show()
            }
        }
        else {
            if (!securityError)
            {
                AlertDialog.Builder(this)
                    .setTitle(R.string.res_no_location_title)
                    .setMessage(R.string.res_no_location_message)
                    .setPositiveButton(
                        R.string.res_accept
                    ) { _, _ ->

                    }
                    .create()
                    .show()
            }
            else {
                AlertDialog.Builder(this)
                    .setTitle(R.string.res_location_no_permissions_title)
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

    @SuppressLint("SetTextI18n")
    private fun setWeather(textWeather: TextView, hourNew: Int) {
        if (preferences.getString("Unit", "Km") == "Km")
            textWeather.text = getString(R.string.res_precipitation) +
                    preferences.getString("precipitationOthers$hourNew", "NODATA") +
                    " mm"
        else
            textWeather.text = getString(R.string.res_precipitation) +
                    (preferences.getString("precipitationOthers$hourNew", "NODATA")!!
                        .toDouble() / 25.4).toString() +
                    " in"
    }

    private fun getOthersData() {
        val editor = preferences.edit()
        val uviValue = metricsData.current.uvi
        val visibilityValue = metricsData.current.visibility
        val windSpeedValue = metricsData.current.wind_speed
        val weatherValue = metricsData.current.weather[0].main
        val precipitationValue = when (weatherValue) {
            "Rain", "Thunderstorm" -> metricsData.current.rain.`1h`
            "Snow" -> metricsData.current.snow.`1h`
            else -> {
                null
            }
        }
        editor.putString("uviOthers0", "$uviValue")
        editor.putString("visibilityOthers0", "$visibilityValue")
        editor.putString("windSpeedOthers0", "$windSpeedValue")
        editor.putString("weatherOthers0", weatherValue)
        if (precipitationValue != null) editor.putString("precipitationOthers0", "$precipitationValue")
        else editor.putString("precipitationOthers0", "NoPrec")
        editor.apply()

        for (index in 0..23) {
            val uviString = "uviOthers${index+1}"
            val visibilityString = "visibilityOthers${index+1}"
            val windSpeedString = "windSpeedOthers${index+1}"
            val precipitationString = "precipitationOthers${index+1}"
            val weatherString = "weatherOthers${index+1}"
            val uviValueForecast = metricsData.hourly[index].uvi
            val visibilityValueForecast = metricsData.hourly[index].visibility
            val windSpeedValueForecast = metricsData.hourly[index].wind_speed
            val weatherValueForecast = metricsData.hourly[index].weather[0].main
            val precipitationValueForecast = when (weatherValueForecast) {
                "Rain" -> metricsData.hourly[index].rain.`1h`
                "Snow" -> metricsData.hourly[index].snow.`1h`
                else -> {
                    null
                }
            }
            editor.putString(uviString, "$uviValueForecast")
            editor.putString(visibilityString, "$visibilityValueForecast")
            editor.putString(windSpeedString, "$windSpeedValueForecast")
            editor.putString(precipitationString, "$precipitationValueForecast")
            editor.putString(weatherString, weatherValueForecast)
            editor.apply()
        }
    }

    private fun getPollutionData() {
        val editor = preferences.edit()
        for (index in 0..24) {
            val tagString = "pollutionMain$index"
            val pollutionValue = pollutionData.list[index].components.pm10
            editor.putString(tagString, "$pollutionValue µg/m³")
            editor.apply()
        }
    }

    private fun getMetricsData() {
        val editor = preferences.edit()
        val temperatureValue = metricsData.current.temp
        val humidityValue = metricsData.current.humidity
        val pressureValue = metricsData.current.pressure
        editor.putString("temperatureMain0", "$temperatureValue")
        editor.putString("humidityMain0", "$humidityValue%")
        editor.putString("pressureMain0", "$pressureValue hPa")
        editor.apply()

        for (index in 0..23) {
            val temperatureString = "temperatureMain${index+1}"
            val humidityString = "humidityMain${index+1}"
            val pressureString = "pressureMain${index+1}"
            val temperatureValueForecast = metricsData.hourly[index].temp
            val humidityValueForecast = metricsData.hourly[index].humidity
            val pressureValueForecast = metricsData.hourly[index].pressure
            editor.putString(temperatureString, "$temperatureValueForecast")
            editor.putString(humidityString, "$humidityValueForecast%")
            editor.putString(pressureString, "$pressureValueForecast hPa")
            editor.apply()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getTime(mode: Int): Pair<String, String?> {
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
        var hourSlider = 0
        if (mode == 1)
            hourSlider = preferences.getFloat("forecastHomeRange", 0F).toInt()
        else if (mode == 2)
            hourSlider = preferences.getFloat("forecastOtherRange", 0F).toInt()
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
            return dayForecastName to timeString
        }
        else
            return dayName to timeString
    }

    private fun getLocation(): Pair<Location?, Boolean> {
        var location: Location? = null
        val providers = locationManager.allProviders
        val securityError = false
        try
        {
            for (i in providers.indices.reversed()) {
                location = locationManager.getLastKnownLocation(providers[i])
                if (location != null) break
            }
        } catch (e: SecurityException) {
            AlertDialog.Builder(this)
                .setTitle(R.string.res_location_no_permissions_title)
                .setMessage(R.string.res_location_no_permissions_message)
                .setPositiveButton(
                    R.string.res_accept
                ) { _, _ ->

                }
                .create()
                .show()
            return null to true
        }
        return location to securityError
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {Log.i("loc", location.latitude.toString() + " " + location.longitude)}
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
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
        else {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0,
                0F,
                locationListener
            )
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
                        0F,
                        locationListener
                    )
                }
                else {
                    AlertDialog.Builder(this)
                        .setTitle(R.string.res_location_no_permissions_title)
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

    private fun setLocale(lan: String) {
        val locale = Locale(lan)
        val res: Resources = resources
        val dm: DisplayMetrics = res.displayMetrics
        val conf: Configuration = res.configuration
        conf.setLocale(locale)
        Locale.setDefault(locale)
        conf.setLayoutDirection(locale)
        res.updateConfiguration(conf, dm)
    }
}