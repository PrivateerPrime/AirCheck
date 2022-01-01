package com.aircheck.ui.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.aircheck.R
import com.aircheck.databinding.FragmentHomeBinding
import com.google.android.material.slider.RangeSlider

class HomeFragment : Fragment() {

//    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_app_bar, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
//        homeViewModel =
//                ViewModelProvider(this).get(HomeViewModel::class.java)

        super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(true)
        val preferences = activity?.getPreferences(Context.MODE_PRIVATE)
        val editor = preferences?.edit()
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

//        val textView: TextView = binding.textHome
//        homeViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })

        val textPollution: TextView = binding.textPollution
        val textTemperature: TextView = binding.textTemperature
        val textHumidity: TextView = binding.textHumidity
        val textPressure: TextView = binding.textPressure
        val textTime: TextView = binding.textTime

        val hour = preferences?.getFloat("forecastRange", 0F)?.toInt()
        textPollution.text = preferences?.getString("pollutionMain$hour", "NODATA")
        var temperature = preferences?.getString("temperatureMain$hour", "0.0")?.toFloat()
        if (preferences?.getString("Temp", "Cel") == "Fah") {
            temperature = ((temperature!! * 9.0/5.0) + 32.0).toFloat()
        }
        val temperatureText = temperature.toString() + if (preferences?.getString("Temp", "Cel") == "Cel")
            getString(R.string.res_celsius)
        else
            getString(R.string.res_fahrenheit)
        textTemperature.text = temperatureText
        textHumidity.text = preferences?.getString("humidityMain$hour", "NODATA")
        textPressure.text = preferences?.getString("pressureMain$hour", "NODATA")

        val day = preferences?.getInt("day$hour", 0)
        var dayName = "REFRESH"
        when (day) {
            1 -> dayName = getString(R.string.res_Sunday)
            2 -> dayName = getString(R.string.res_Monday)
            3 -> dayName = getString(R.string.res_Tuesday)
            4 -> dayName = getString(R.string.res_Wednesday)
            5 -> dayName = getString(R.string.res_Thursday)
            6 -> dayName = getString(R.string.res_Friday)
            7 -> dayName = getString(R.string.res_Saturday)
        }
        textTime.text = "$dayName, ${preferences?.getString("time$hour", "NODATA")}"

        val rangeSlider: RangeSlider = binding.sliderHome
        rangeSlider.values = listOf(preferences?.getFloat("forecastRange", 0F))
        rangeSlider.addOnChangeListener {
                _, _, _ ->
            val values = rangeSlider.values
            editor?.putFloat("forecastRange", values[0])
            editor?.apply()
            val hourNew = values[0].toInt()
            textPollution.text = preferences?.getString("pollutionMain$hourNew", "NODATA")
            var temperature2 = preferences?.getString("temperatureMain$hourNew", "0.0")?.toFloat()
            if (preferences?.getString("Temp", "Cel") == "Fah") {
                temperature2 = ((temperature2!! * 9.0/5.0) + 32.0).toFloat()
            }
            val temperatureText2 = temperature2.toString() + if (preferences?.getString("Temp", "Cel") == "Cel")
                getString(R.string.res_celsius)
            else
                getString(R.string.res_fahrenheit)
            textTemperature.text = temperatureText2
            textHumidity.text = preferences?.getString("humidityMain$hourNew", "NODATA")
            textPressure.text = preferences?.getString("pressureMain$hourNew", "NODATA")

            val day2 = preferences?.getInt("day$hourNew", 0)
            var dayName2 = "REFRESH"
            when (day2) {
                0 -> dayName2 = getString(R.string.res_Saturday)
                1 -> dayName2 = getString(R.string.res_Sunday)
                2 -> dayName2 = getString(R.string.res_Monday)
                3 -> dayName2 = getString(R.string.res_Tuesday)
                4 -> dayName2 = getString(R.string.res_Wednesday)
                5 -> dayName2 = getString(R.string.res_Thursday)
                6 -> dayName2 = getString(R.string.res_Friday)
                7 -> dayName2 = getString(R.string.res_Saturday)
            }
            textTime.text = "$dayName2, ${preferences?.getString("time$hourNew", "NODATA")}"
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}