package com.example.sampleapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sampleapp.data.Country

class PreviewCountryModel : ViewModel() {
    private val _countries = MutableLiveData<List<Country>>()
    val countries: LiveData<List<Country>> get() = _countries

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    init {
        // Provide dummy data
        _countries.value = listOf(
            Country().apply {
                name = Country.Name().apply { common = "France" }
                capital = listOf("Paris")
            },
            Country().apply {
                name = Country.Name().apply { common = "Germany" }
                capital = listOf("Berlin")
            }
        )
        _isLoading.value = false
    }
}