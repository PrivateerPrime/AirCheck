package com.aircheck.ui.other

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.aircheck.R
import com.aircheck.databinding.FragmentOtherBinding

class OtherFragment : Fragment() {

    //private lateinit var otherViewModel: OtherViewModel
    private var _binding: FragmentOtherBinding? = null

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
//        otherViewModel =
//                ViewModelProvider(this).get(OtherViewModel::class.java)

        super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(true)
        _binding = FragmentOtherBinding.inflate(inflater, container, false)
        val root: View = binding.root

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

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}