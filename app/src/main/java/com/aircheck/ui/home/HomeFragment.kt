package com.aircheck.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.aircheck.MainActivity
import com.aircheck.R
import com.aircheck.databinding.FragmentHomeBinding
import com.google.android.material.slider.RangeSlider

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!
    private lateinit var preferences: SharedPreferences

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_app_bar, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        Log.i("t", "klikRefreshHome")
        (activity as MainActivity).getDataHome()
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(true)
        preferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val editor = preferences.edit()
        _binding = FragmentHomeBinding.inflate(inflater, container, false)


        val textPollution = binding.textPollution
        val textTemperature = binding.textTemperature
        val textHumidity = binding.textHumidity
        val textPressure = binding.textPressure
        val textTime = binding.textHomeTime

        val hour = preferences.getFloat("forecastHomeRange", 0F).toInt()
        setData(hour, textPollution, textTemperature, textHumidity, textPressure, textTime)

        val rangeSlider: RangeSlider = binding.sliderHome
        rangeSlider.values = listOf(preferences.getFloat("forecastHomeRange", 0F))
        rangeSlider.addOnChangeListener {
                _, _, _ ->
            val values = rangeSlider.values
            editor?.putFloat("forecastHomeRange", values[0])
            editor?.apply()
            val hourNew = values[0].toInt()
            setData(hourNew, textPollution, textTemperature, textHumidity, textPressure, textTime)
        }
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun setData(hour: Int, textPollution: TextView, textTemperature: TextView, textHumidity: TextView, textPressure: TextView, textTime: TextView) {
        textPollution.text = preferences.getString("pollutionMain$hour", "NODATA")
        try {
            var temperature = preferences.getString("temperatureMain$hour", "NODATA")?.toFloat()
            if (preferences.getString("Temp", "Cel") == "Fah") {
                temperature = ((temperature!! * 9.0/5.0) + 32.0).toFloat()
            }
            val temperatureText = temperature.toString() + if (preferences.getString("Temp", "Cel") == "Cel")
                getString(R.string.res_celsius)
            else
                getString(R.string.res_fahrenheit)
            textTemperature.text = temperatureText
        }
        catch (e: NumberFormatException) {
            textTemperature.text = "NODATA"
        }
        textHumidity.text = preferences.getString("humidityMain$hour", "NODATA")
        textPressure.text = preferences.getString("pressureMain$hour", "NODATA")

        val day = preferences.getInt("day$hour", -1)
        var dayName = "REFRESH"
        when (day) {
            0 -> dayName = getString(R.string.res_Saturday)
            1 -> dayName = getString(R.string.res_Sunday)
            2 -> dayName = getString(R.string.res_Monday)
            3 -> dayName = getString(R.string.res_Tuesday)
            4 -> dayName = getString(R.string.res_Wednesday)
            5 -> dayName = getString(R.string.res_Thursday)
            6 -> dayName = getString(R.string.res_Friday)
            7 -> dayName = getString(R.string.res_Saturday)
        }
        textTime.text = "$dayName, ${preferences.getString("time$hour", "NODATA")}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}