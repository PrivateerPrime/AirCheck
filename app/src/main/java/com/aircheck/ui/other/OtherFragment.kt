package com.aircheck.ui.other

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.aircheck.MainActivity
import com.aircheck.R
import com.aircheck.databinding.FragmentOtherBinding
import com.google.android.material.slider.RangeSlider

class OtherFragment : Fragment() {

    private var _binding: FragmentOtherBinding? = null
    private lateinit var preferences: SharedPreferences

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_app_bar, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        Log.i("t", "klikRefreshOthers")
        (activity as MainActivity).getDataOthers()
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(true)
        preferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val editor = preferences.edit()
        _binding = FragmentOtherBinding.inflate(inflater, container, false)

        val pollution1: TextView = binding.textOtherPollution1
        val pollution2: TextView = binding.textOtherPollution2
        val pollution3: TextView = binding.textOtherPollution3

        pollution1.text = "11 µg/m³"
        pollution2.text = "12 µg/m³"
        pollution3.text = "13 µg/m³"

        val weather1: TextView = binding.textOtherWeather1
        val weather2: TextView = binding.textOtherWeather2
        val weather3: TextView = binding.textOtherWeather3

        weather1.text = "11 °C\n61%\n1021 hPa"
        weather2.text = "12 °C\n62%\n1022 hPa"
        weather3.text = "13 °C\n63%\n1023 hPa"

//        val rangeSlider: RangeSlider = binding.sliderOther
//        rangeSlider.values = listOf(preferences.getFloat("forecastRange", 0F))
//        rangeSlider.addOnChangeListener {
//                _, _, _ ->
//            val values = rangeSlider.values
//            editor?.putFloat("forecastRange", values[0])
//            editor?.apply()
//            val hourNew = values[0].toInt()
//            textPollution.text = preferences.getString("pollutionMain$hourNew", "NODATA")
//            try {
//                var temperature2 = preferences.getString("temperatureMain$hourNew", "0.0")?.toFloat()
//                if (preferences.getString("Temp", "Cel") == "Fah") {
//                    temperature2 = ((temperature2!! * 9.0 / 5.0) + 32.0).toFloat()
//                }
//                val temperatureText2 = temperature2.toString() + if (preferences.getString("Temp", "Cel") == "Cel")
//                    getString(R.string.res_celsius)
//                else
//                    getString(R.string.res_fahrenheit)
//                textTemperature.text = temperatureText2
//            }
//            catch (e: NumberFormatException) {
//                textTemperature.text = "NODATA"
//            }
//            textHumidity.text = preferences.getString("humidityMain$hourNew", "NODATA")
//            textPressure.text = preferences.getString("pressureMain$hourNew", "NODATA")
//
//            val day2 = preferences.getInt("day$hourNew", -1)
//            var dayName2 = "REFRESH"
//            when (day2) {
//                0 -> dayName2 = getString(R.string.res_Saturday)
//                1 -> dayName2 = getString(R.string.res_Sunday)
//                2 -> dayName2 = getString(R.string.res_Monday)
//                3 -> dayName2 = getString(R.string.res_Tuesday)
//                4 -> dayName2 = getString(R.string.res_Wednesday)
//                5 -> dayName2 = getString(R.string.res_Thursday)
//                6 -> dayName2 = getString(R.string.res_Friday)
//                7 -> dayName2 = getString(R.string.res_Saturday)
//            }
//            textTime.text = "$dayName2, ${preferences.getString("time$hourNew", "NODATA")}"
//        }




        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}