package com.aircheck.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "14 µg/m³"
    }
    private val _text2 = MutableLiveData<String>().apply {
        value = "16 °C "
    }
    private val _text3 = MutableLiveData<String>().apply {
        value = "68%"
    }
    private val _text4 = MutableLiveData<String>().apply {
        value = "1023 hPa"
    }
    val text: LiveData<String> = _text
    val text2: LiveData<String> = _text2
    val text3: LiveData<String> = _text3
    val text4: LiveData<String> = _text4
}