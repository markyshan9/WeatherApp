package com.example.weatherapp.adapters

data class HourWeather(
    val cityName: String,
    val time: String,
    val condition: String,
    val imageUrlCondition: String,
    val currentTemp: String,
)
