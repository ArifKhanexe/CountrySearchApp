package com.example.sampleapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sampleapp.R
import com.example.sampleapp.data.Country
import com.example.sampleapp.retrofit.RetrofitInstance
import com.example.sampleapp.retrofit.ServiceInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivityViewModel : ViewModel() {


    private val _images = MutableLiveData<List<Int>>()
    val images: LiveData<List<Int>> get() = _images

    private val _currentPosition = MutableLiveData<Int>()
    val currentPosition: LiveData<Int> get() = _currentPosition


    init {
        _currentPosition.value = 0
        loadImages()

    }


    private fun loadImages() {
        // Load local images into the list
        val imageList = listOf(
            R.drawable.image1,
            R.drawable.image2,
            R.drawable.image3,
            R.drawable.image4,
            R.drawable.image5

        )
        _images.value = imageList
    }

    fun updatePosition(position: Int) {
        _currentPosition.value = position
    }

}