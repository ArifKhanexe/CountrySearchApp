package com.example.sampleapp.data

import com.example.sampleapp.retrofit.RetrofitInstance
import com.example.sampleapp.retrofit.ServiceInterface

class CountryRepository {
    private val countryApi = RetrofitInstance.retrofit.create(ServiceInterface::class.java)

    suspend fun getCountries(): List<Country> {
        return countryApi.getAllCountries()
    }
}