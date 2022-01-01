package com.aircheck.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
//        homeViewModel =
//                ViewModelProvider(this).get(HomeViewModel::class.java)

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
        textTime.text = preferences?.getString("time$hour", "NODATA")

        val rangeSlider: RangeSlider = binding.sliderHome
        rangeSlider.values = listOf(preferences?.getFloat("forecastRange", 0F))
        rangeSlider.addOnChangeListener {
                _, _, _ ->
            val values = rangeSlider.values
            editor?.putFloat("forecastRange", values[0])
            editor?.apply()
            val hourTemp = values[0].toInt()
            textPollution.text = preferences?.getString("pollutionMain$hourTemp", "NODATA")
            var temperature2 = preferences?.getString("temperatureMain$hourTemp", "0.0")?.toFloat()
            if (preferences?.getString("Temp", "Cel") == "Fah") {
                temperature2 = ((temperature2!! * 9.0/5.0) + 32.0).toFloat()
            }
            val temperatureText2 = temperature2.toString() + if (preferences?.getString("Temp", "Cel") == "Cel")
                getString(R.string.res_celsius)
            else
                getString(R.string.res_fahrenheit)
            textTemperature.text = temperatureText2
            textHumidity.text = preferences?.getString("humidityMain$hourTemp", "NODATA")
            textPressure.text = preferences?.getString("pressureMain$hourTemp", "NODATA")
            textTime.text = preferences?.getString("time$hourTemp", "NODATA")
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}