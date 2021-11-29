package com.aircheck.ui.settings

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.aircheck.R
import com.aircheck.databinding.FragmentSettingsBinding
import com.google.android.material.button.MaterialButtonToggleGroup
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

        val preferences = activity?.getPreferences(Context.MODE_PRIVATE)
        val editor = preferences?.edit()

        val rangeSlider: RangeSlider = binding.sliderRange
        val rangeTextView: TextView = binding.textSearchRange

        rangeTextView.text = getString(R.string.res_search_range) + (" ${rangeSlider.values[0].toString()} KM")

            rangeSlider.addOnChangeListener {
                    _, _, _ ->
                val values = rangeSlider.values
                rangeTextView.text = getString(R.string.res_search_range) + (" ${values[0].toString()} KM")

        }

        val units1Button: MaterialButtonToggleGroup = binding.toggleGroupUnits1
        units1Button.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            if (isChecked){
                when (checkedId) {
                    binding.buttonKm.id -> {
                        editor?.putString("Unit", "Km")
                        Log.i("UNIT", "KM")
                        editor?.apply()
                    }
                    binding.buttonMil.id -> {
                        editor?.putString("Unit", "Mil")
                        Log.i("UNIT", "Mil")
                        editor?.apply()
                    }
                }

            }
        }
        val kmButton: Button = binding.buttonKm
        val milButton: Button = binding.buttonMil
        val selection = preferences?.getString("Unit", null)
        if(selection == "Mil")
            units1Button.check(milButton.id);
        else
            units1Button.check(kmButton.id);

//        editor?.putString("Temp", "Celc")
//        editor?.putString("Lang", "ENG")
//        editor?.apply()

        //TODO Dodanie zmiany języka
        //https://developer.android.com/training/data-storage/shared-
//        https://developer.android.com/guide/topics/ui/settings/use-saved-values

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