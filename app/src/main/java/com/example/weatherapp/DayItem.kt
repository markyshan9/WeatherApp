package com.example.weatherapp

data class DayItem(
    val cityName: String,
    val time: String,
    val condition: String,
    val imageUrlCondition: String,
    val currentTemp: String,
    val maxTemp: String,
    val minTemp: String,
    val hours: String
)
