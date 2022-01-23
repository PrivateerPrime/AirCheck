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

    private fun setTestData(testNumber: Int){
        var pollution: String? = ""
        var temperature: String? = ""
        var humidity: String? = ""
        var pressure: String? = ""
        var day = 0
        var hour: String? = ""

        when(testNumber){
            1 -> { //Small values
                pollution = "0"
                temperature = "0"
                humidity = "0"
                pressure = "0"
                day = 1
                hour = "00:00"
            }
            2 -> { //Big values
                pollution = "1000"
                temperature = "1000"
                humidity = "1000"
                pressure = "1000"
                day = 1
                hour = "00:00"
            }
            3 -> { //Extremely big values
                pollution = "100000000000"
                temperature = "100000000000"
                humidity = "100000000000"
                pressure = "100000000000"
                day = 1
                hour = "00:00"
            }
            4 -> { //Out of bounds day values
                pollution = "0"
                temperature = "0"
                humidity = "0"
                pressure = "0"
                day = -10
                hour = "00:00"
            }
            5 -> { //Out of bounds day values pt. 2
                pollution = "0"
                temperature = "0"
                humidity = "0"
                pressure = "0"
                day = 100
                hour = "00:00"
            }
            6 -> { //Out of bounds hour values
                pollution = "0"
                temperature = "0"
                humidity = "0"
                pressure = "0"
                day = 1
                hour = "-1:00"
            }
            7 -> { //Out of bounds hour values pt. 2
                pollution = "0"
                temperature = "0"
                humidity = "0"
                pressure = "0"
                day = 1
                hour = "30:70"
            }
            8 -> { //Out of bounds hour values pt. 3
                pollution = "0"
                temperature = "0"
                humidity = "0"
                pressure = "0"
                day = 1
                hour = "100:100"
            }
            9 -> { //Nonnumerical values
                pollution = "abc"
                temperature = "abc"
                humidity = "abc"
                pressure = "abc"
                day = 1
                hour = "00:00"
            }
            10 -> { //Nonnumerical hour values
                pollution = "0"
                temperature = "0"
                humidity = "0"
                pressure = "0"
                day = 1
                hour = "aa:bb"
            }
            11 -> { //Empty data
                pollution = ""
                temperature = ""
                humidity = ""
                pressure = ""
                day = 1
                hour = "00:00"
            }
            12 -> { //Empty hour data
                pollution = "0"
                temperature = "0"
                humidity = "0"
                pressure = "0"
                day = 1
                hour = ""
            }
            13 -> { //Null values
                pollution = null
                temperature = null
                humidity = null
                pressure = null
                day = 1
                hour = "00:00"
            }
            14 -> { //Null hour values
                pollution = "0"
                temperature = "0"
                humidity = "0"
                pressure = "0"
                day = 1
                hour = null
            }
            15 -> { //Special characters - newlines
                pollution = "0\n0"
                temperature = "0\n0"
                humidity = "0\n0"
                pressure = "0\n0"
                day = 1
                hour = "00:00"
            }
            16 -> { //Special characters - whitespaces
                pollution = "0\t0\b0"
                temperature = "0\t0\b0"
                humidity = "0\t0\b0"
                pressure = "0\t0\b0"
                day = 1
                hour = "00:00"
            }
            17 -> { //Special hour characters - newlines
                pollution = "0"
                temperature = "0"
                humidity = "0"
                pressure = "0"
                day = 1
                hour = "00:00"
            }
            18 -> { //Special hour characters - whitespaces
                pollution = "0"
                temperature = "0"
                humidity = "0"
                pressure = "0"
                day = 1
                hour = "0\t0:0\b0"
            }
            19 -> { //Float values
                pollution = "25.4"
                temperature = "14.9"
                humidity = "38.1"
                pressure = "76.2"
                day = 1
                hour = "00:00"
            }
            20 -> { //Long fractional parts
                pollution = "2.11223344"
                temperature = "2.11223344"
                humidity = "2.11223344"
                pressure = "2.11223344"
                day = 1
                hour = "00:00"
            }
            21 -> { //Regular values
                pollution = "56.7"
                temperature = "12"
                humidity = "95.71"
                pressure = "1007.4"
                day = 1
                hour = "00:00"
            }
            22 -> { //Regular time values - wednesday afternoon
                pollution = "0"
                temperature = "0"
                humidity = "0"
                pressure = "0"
                day = 4
                hour = "16:37"
            }
            23 -> { //Regular time values - saturday night
                pollution = "0"
                temperature = "0"
                humidity = "0"
                pressure = "0"
                day = 7
                hour = "23:12"
            }
        }

        val editor = preferences.edit()

        editor?.putString("pollutionMain$testNumber", pollution)
        editor?.putString("temperatureMain$testNumber", temperature)
        editor?.putString("humidityMain$testNumber", humidity)
        editor?.putString("pressureMain$testNumber", pressure)
        editor?.putInt("day$testNumber", day)
        editor?.putString("time$testNumber", hour)
        editor?.apply()
    }

    @SuppressLint("SetTextI18n")
    private fun setData(hour: Int, textPollution: TextView, textTemperature: TextView, textHumidity: TextView, textPressure: TextView, textTime: TextView) {
        setTestData(hour)
        val pollutionVal = preferences.getString("pollutionMain$hour", "NODATA")?.toFloatOrNull()
        if(pollutionVal == null)
            textPollution.text = "NODATA"
        else
            textPollution.text = pollutionVal.toString()
        //textPollution.text = preferences.getString("pollutionMain$hour", "NODATA")
        try {3
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