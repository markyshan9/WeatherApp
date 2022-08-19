package com.example.weatherapp.adapters

data class WeatherModel(
    val cityName: String,
    val time: String,
    val condition: String,
    val imageUrlCondition: String,
    val currentTemp: String,
    val maxTemp: String,
    val minTemp: String,
    val hours: String
)
