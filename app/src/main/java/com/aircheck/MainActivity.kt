package com.aircheck

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.aircheck.databinding.ActivityMainBinding
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.StringRequest
import java.lang.Exception
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var requestQueue: RequestQueue;

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_app_bar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        val url = "https://api.openweathermap.org/data/2.5/weather?lat=35&lon=39&appid=b06c7b5f09d3a79aa795615537b676c5"
        val stringRequest = StringRequest(Request.Method.GET, url,
            {
                    response -> print(response)
            }, {
                    error ->  print(error)
            }
        )
        try {
            requestQueue.add(stringRequest)
        }
        catch (e: Exception) {
            print(e.stackTrace)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
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
}