package com.example.sampleapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sampleapp.data.Country
import com.example.sampleapp.data.CountryRepository
import kotlinx.coroutines.launch

class CountryViewModel(private val repository: CountryRepository) : ViewModel() {

    private val _countries = MutableLiveData<List<Country>>()
    val countries: LiveData<List<Country>> get() = _countries

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun fetchCountries() {
        viewModelScope.launch {
            _isLoading.value = true  // Show progress bar
            try {
                val countryList = repository.getCountries()
                _countries.value = countryList
            } catch (e: Exception) {

            } finally {
                _isLoading.value = false  // Hide progress bar
            }
        }
    }
}