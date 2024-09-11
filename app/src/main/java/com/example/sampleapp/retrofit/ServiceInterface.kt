package com.example.sampleapp.retrofit

import com.example.sampleapp.data.Country
import retrofit2.http.GET

interface ServiceInterface {
    @GET("all")
    suspend fun getAllCountries(): List<Country>
}