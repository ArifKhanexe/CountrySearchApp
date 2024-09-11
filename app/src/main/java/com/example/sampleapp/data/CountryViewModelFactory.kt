package com.example.sampleapp.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sampleapp.viewmodel.CountryViewModel

class CountryViewModelFactory(private val repository: CountryRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CountryViewModel::class.java)) {
            return CountryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}