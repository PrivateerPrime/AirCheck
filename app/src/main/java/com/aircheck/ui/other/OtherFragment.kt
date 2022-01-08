package com.aircheck.ui.other

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.aircheck.MainActivity
import com.aircheck.R
import com.aircheck.databinding.FragmentOtherBinding
import com.google.android.material.slider.RangeSlider
import java.math.RoundingMode

class OtherFragment : Fragment() {

    private var _binding: FragmentOtherBinding? = null

    private val binding get() = _binding!!
    private lateinit var preferences: SharedPreferences

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_app_bar, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        Log.i("t", "klikRefreshOther")
        (activity as MainActivity).getDataOthers()
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
        _binding = FragmentOtherBinding.inflate(inflater, container, false)

        val textWind = binding.textWind
        val textWeather = binding.textWeather
        val textUv = binding.textUv
        val textVisibility = binding.textVisibility
        val textTime = binding.textOtherTime
        val imageWeather = binding.imageWeather

        val hour = preferences.getFloat("forecastOtherRange", 0F).toInt()
        setData(hour, textWind, textWeather, textUv, textVisibility, textTime, imageWeather)

        val rangeSlider: RangeSlider = binding.sliderOther
        rangeSlider.values = listOf(preferences.getFloat("forecastOtherRange", 0F))
        rangeSlider.addOnChangeListener {
            _, _, _ ->
            val values = rangeSlider.values
            editor?.putFloat("forecastOtherRange", values[0])
            editor?.apply()
            val hourNew = values[0].toInt()
            setData(hourNew, textWind, textWeather, textUv, textVisibility, textTime, imageWeather)
        }

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun setData(hour: Int, textWind: TextView, textWeather: TextView, textUv: TextView, textVisibility: TextView, textTime: TextView ,imageWeather: ImageView) {
        if (preferences.getString("windSpeedOthers$hour", "NODATA") != "NODATA")
        {
            if (preferences.getString("Unit", "Km") == "Km") {
                textWind.text = "${preferences.getString("windSpeedOthers$hour", "NODATA")} m/s"
                textVisibility.text = "${(preferences.getString("visibilityOthers$hour", "NODATA")!!.toDouble() / 1000).toBigDecimal().setScale(1,RoundingMode.HALF_EVEN)} km"
            }
            else {
                textWind.text = "${(preferences.getString("windSpeedOthers$hour", "NODATA")!!.toDouble() / 0.44704).toBigDecimal().setScale(2,RoundingMode.HALF_EVEN)} mph"
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}