package com.aircheck.ui.settings

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.aircheck.R
import com.aircheck.databinding.FragmentSettingsBinding
import com.google.android.material.button.MaterialButtonToggleGroup
import android.content.res.Resources
import android.util.DisplayMetrics
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*

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
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val preferences = activity?.getPreferences(Context.MODE_PRIVATE)
        val editor = preferences?.edit()

        val kmButtonId = binding.buttonKm.id
        val milButtonId = binding.buttonMil.id
        val celButtonId = binding.buttonCelsius.id
        val fahButtonId = binding.buttonFahrenheit.id
        val polButtonId = binding.buttonPolish.id
        val engButtonId = binding.buttonEnglish.id
        val units1Group: MaterialButtonToggleGroup = binding.toggleGroupUnits1
        val units2Group: MaterialButtonToggleGroup = binding.toggleGroupUnits2
        val languageGroup: MaterialButtonToggleGroup = binding.toggleGroupLanguage
        val language = preferences?.getString("Lan", null)
        units1Group.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked){
                when (checkedId) {
                    kmButtonId -> {
                        editor?.putString("Unit", "Km")
                        Log.i("UNIT", "Km")
                        editor?.apply()
                    }
                    milButtonId -> {
                        editor?.putString("Unit", "Mil")
                        Log.i("UNIT", "Mil")
                        editor?.apply()
                    }
                }
            }
        }

        units2Group.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked){
                when (checkedId) {
                    celButtonId -> {
                        editor?.putString("Temp", "Cel")
                        Log.i("TEMP", "Cel")
                        editor?.apply()
                    }
                    fahButtonId -> {
                        editor?.putString("Temp", "Fah")
                        Log.i("TEMP", "Fah")
                        editor?.apply()
                    }
                }
            }
        }

        languageGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked){
                when (checkedId) {
                    polButtonId -> {
                        editor?.putString("Lan", "pl")
                        Log.i("LAN", "pl")
                        editor?.apply()
                    }
                    engButtonId -> {
                        editor?.putString("Lan", "en")
                        Log.i("LAN", "en")
                        editor?.apply()
                    }
                }
            }
        }


        binding.buttonEnglish.setOnClickListener {
            if (language != "en")
                addAlertDialog("en")
        }
        binding.buttonPolish.setOnClickListener {
            if (language != "pl")
                addAlertDialog("pl")
        }

        val selectionUnit1 = preferences?.getString("Unit", "Km")
        val selectionUnit2 = preferences?.getString("Temp", "Cel")
        val selectionLanguage = preferences?.getString("Lan", "en")
        if(selectionUnit1 == "Mil")
            units1Group.check(milButtonId)
        else
            units1Group.check(kmButtonId)
        if(selectionUnit2 == "Fah")
            units2Group.check(fahButtonId)
        else
            units2Group.check(celButtonId)
        if(selectionLanguage == "pl")
            languageGroup.check(polButtonId)
        else
            languageGroup.check(engButtonId)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setLocale(lang: String) {
        val res: Resources = requireContext().resources
        val dm: DisplayMetrics = res.displayMetrics
        val conf: android.content.res.Configuration = res.configuration
        conf.setLocale(Locale(lang.lowercase()))
        res.updateConfiguration(conf, dm)

        val refresh = activity?.intent
        activity?.finish()
        startActivity(refresh)
    }

    private fun addAlertDialog(lang: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.res_restart_title)
            .setMessage(R.string.res_restart_text)
            .setNeutralButton(R.string.res_cancel) { _, _ ->
                if (lang == "pl")
                    binding.toggleGroupLanguage.check(binding.buttonEnglish.id)
                else
                    binding.toggleGroupLanguage.check(binding.buttonPolish.id)
            }
            .setPositiveButton(R.string.res_accept) { _, _ ->
                setLocale(lang)
            }
            .show()
    }

}