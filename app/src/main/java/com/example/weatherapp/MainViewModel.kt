package com.example.weatherapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weatherapp.adapters.WeatherModel

class MainViewModel : ViewModel() {

    val liveDataCurrent = MutableLiveData<WeatherModel>()


    val liveDataListDay = MutableLiveData<List<WeatherModel>>()


    val liveDataListHour = MutableLiveData<List<WeatherModel>>()

    val cordLat = MutableLiveData<String>()

    val cordLong = MutableLiveData<String>()



}