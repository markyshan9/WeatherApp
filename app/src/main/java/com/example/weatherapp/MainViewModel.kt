package com.example.weatherapp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weatherapp.adapters.WeatherModel


// TODO это не так важно для Junior уровня, но лучше сразу делать качетственную системную организацию пакетов.
/*
* Представь что будет, если приложение разрастется. Ты получишь 2 пакета fragments и adapters с огромным количеством классов.
* Суть пакетов тогда потеряется. Если упростить, то клссы должны лежать по СЕМАНТИЧЕСКОЙ близости, т.е. по смыслу, а не по схожести названий.
* В случае твоего простого приложеня иерархия будет примерно такая:
* weatherapp
*     days
*     hours
*
* внутри days будет к примеру DaysFragment и DaysAdapter
* */
class MainViewModel : ViewModel() {

    val liveDataCurrent = MutableLiveData<WeatherModel>()


    val liveDataListDay = MutableLiveData<List<WeatherModel>>()


    val liveDataListHour = MutableLiveData<List<WeatherModel>>()



}