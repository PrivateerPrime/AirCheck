package com.aircheck.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.aircheck.R
import com.aircheck.databinding.FragmentSettingsBinding
import com.google.android.material.slider.RangeSlider

class SettingsFragment : Fragment() {

//    private lateinit var settingsViewModel: SettingsViewModel
    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
//        settingsViewModel =
//                ViewModelProvider(this).get(SettingsViewModel::class.java)

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

//        val textView: TextView = binding.textSearchRange
//        settingsViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })

        val rangeSlider: RangeSlider = binding.sliderRange
        val rangeTextView: TextView = binding.textSearchRange

        rangeTextView.text = getString(R.string.res_search_range) + (" ${rangeSlider.values[0].toString()} KM")

            rangeSlider.addOnChangeListener {
                    _, _, _ ->
                val values = rangeSlider.values
                rangeTextView.text = getString(R.string.res_search_range) + (" ${values[0].toString()} KM")

        }

        //TODO Dodanie zmiany języka
        //https://developer.android.com/training/data-storage/shared-preferences

//        rangeSlider.addOnSliderTouchListener(object: RangeSlider.OnSliderTouchListener{
//        @SuppressLint("LongLogTag")
//        override fun onStartTrackingTouch(slider: RangeSlider) {
//            val values = rangeSlider.values
//            Log.i("SliderPreviousValueFrom", values[0].toString())
//            Log.i("SliderPreviousValue To", values[1].toString())
//        }
//
//        override fun onStopTrackingTouch(slider: RangeSlider) {
//            val values = rangeSlider.values
//            Log.i("SliderNewValue From", values[0].toString())
//            Log.i("SliderNewValue To", values[1].toString())
//
//            rangeTextView.text = getString(R.string.res_search_range) + values[1]
//        }
//    })
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}