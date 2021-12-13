package com.aircheck

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
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

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var requestQueue: RequestQueue
    private lateinit var installationData: InstallationData
    private lateinit var preferences: SharedPreferences

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_app_bar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /* TODO Dodać jeszcze jedno zapytanie API zwracające temperaturę, wilgotność, ciśnienie na następne 24 godziny
       TODO Dodać GPS
       TODO Dodać tekst pokazujący godzinę/datę/czas od ostatniej synhcronizacji
       TODO Dodać obsługę maxDistanceKM (linia49)
    */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val url = "https://airapi.airly.eu/v2/measurements/nearest?lat=50.062006&lng=19.940984&maxDistanceKM=5"
        val stringRequest = object: StringRequest(Method.GET, url,
            {
                    response -> Log.i("resp", response)
                    installationData = Gson().fromJson(response, InstallationData::class.java)
                    setMainData()
            }, {
                    error ->  Log.e("resp", error.toString())
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

        findViewById<TextView>(R.id.text_pollution).text = indexPollution.toString() + " µg/m³"
        findViewById<TextView>(R.id.text_temperature).text = indexTemperature.toString() + " °C"
        findViewById<TextView>(R.id.text_humidity).text = indexHumidity.toString() + "%"
        findViewById<TextView>(R.id.text_pressure).text = indexPressure.toString() + " hPa"
    }
}