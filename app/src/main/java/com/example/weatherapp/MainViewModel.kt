package com.example.weatherapp

import android.content.Context
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherapp.adapters.WeatherModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONArray
import org.json.JSONObject


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
const val API_KEY = "f991625e72fa4aacae2143634220809"

class MainViewModel : ViewModel() {



    private val _liveDataCurrent = MutableLiveData<WeatherModel>()
    var liveDataCurrent: LiveData<WeatherModel> = _liveDataCurrent

    private val _liveDataListDay = MutableLiveData<List<WeatherModel>>()
    var liveDataListDay: LiveData<List<WeatherModel>> = _liveDataListDay

    private val _liveDataListHour = MutableLiveData<List<WeatherModel>>()
    var liveDataListHour: LiveData<List<WeatherModel>> = _liveDataListHour

    private val _liveDataWeatherBoolean = MutableLiveData<Boolean>()
    var liveDataWeatherBoolean: LiveData<Boolean> = _liveDataWeatherBoolean


    fun requestWeatherData(cityName: String, reset: Boolean, context: Context) {
        val url = "https://api.weatherapi.com/v1/forecast.json?" +
                "key=" +
                API_KEY +
                "&q=" +
                cityName +
                "&days=10&aqi=no&alerts=no"
        val queue = Volley.newRequestQueue(context)
        val request = StringRequest(
            Request.Method.GET,
            url,
            { result ->
                parseWeatherData(result, reset)
                _liveDataWeatherBoolean.value = true
            },
            { error ->
                Toast.makeText(context, "Please enter a valid city name", Toast.LENGTH_SHORT).show()
                _liveDataWeatherBoolean.value = false
            }
        )

        queue.add(request)
    }


    private fun parseWeatherData(result: String, reset: Boolean) {
        val mainObject = JSONObject(result)
        val listDays: List<WeatherModel> = parseDays(mainObject = mainObject)
        val currentWeather = parseCurrentData(mainObject = mainObject, weatherItem = listDays[0])
        val listHours = parseHours(currentWeather.hours)
        if (_liveDataListDay.value == null || reset)
            _liveDataListDay.value = listDays.subList(1, listDays.size)
        if (_liveDataCurrent.value == null || reset)
            _liveDataCurrent.value = currentWeather
        if (_liveDataListHour.value == null || reset)
            _liveDataListHour.value = listHours

    }


    private fun parseCurrentData(mainObject: JSONObject, weatherItem: WeatherModel): WeatherModel {
        return WeatherModel(
            cityName = mainObject.getJSONObject("location").getString("name"),
            time = mainObject.getJSONObject("current").getString("last_updated"),
            condition = mainObject.getJSONObject("current").getJSONObject("condition")
                .getString("text"),
            imageUrlCondition = mainObject.getJSONObject("current").getJSONObject("condition")
                .getString("icon"),
            currentTemp = mainObject.getJSONObject("current").getString("temp_c"),
            weatherItem.maxTemp,
            weatherItem.minTemp,
            weatherItem.hours
        )
    }

    private fun parseDays(mainObject: JSONObject): List<WeatherModel> {
        val list = ArrayList<WeatherModel>()
        val daysArray = mainObject.getJSONObject("forecast")
            .getJSONArray("forecastday")
        val name = mainObject.getJSONObject("location").getString("name")

        for (i in 0 until daysArray.length()) {
            val day = daysArray[i] as JSONObject
            val itemDay = WeatherModel(
                cityName = name,
                time = day.getString("date"),
                condition = day.getJSONObject("day").getJSONObject("condition")
                    .getString("text"),
                imageUrlCondition = day.getJSONObject("day").getJSONObject("condition")
                    .getString("icon"),
                currentTemp = "",
                maxTemp = day.getJSONObject("day").getString("maxtemp_c").toFloat().toInt()
                    .toString(),
                minTemp = day.getJSONObject("day").getString("mintemp_c").toFloat().toInt()
                    .toString(),
                hours = day.getJSONArray("hour").toString()


            )
            list.add(itemDay)
        }
        return list
    }


    private fun parseHours(hours: String): List<WeatherModel> {
        val list = ArrayList<WeatherModel>()
        val hoursArray = JSONArray(hours)
        for (i in 0 until hoursArray.length()) {
            val hour = hoursArray[i] as JSONObject
            val itemHour = WeatherModel(
                cityName = "",
                time = hour.getString("time"),
                condition = hour.getJSONObject("condition").getString("text"),
                imageUrlCondition = hour.getJSONObject("condition").getString("icon"),
                currentTemp = hour.getString("temp_c"),
                maxTemp = "", //todo судя по всему у тебя должно быть две модели HourWeather и DayWeather.
                // Об этом говорит то что у тебя некоторые  поля пустые и не нужные. Зачем в модели для часа поле "hours"? ну и тд.
                minTemp = "",
                hours = ""
            )
            list.add(itemHour)
        }
        return list
    }

    fun parseHoursByDay(dayWeather: WeatherModel) {
        val list = ArrayList<WeatherModel>()
        val hoursArray = JSONArray(dayWeather.hours)
        for (i in 0 until hoursArray.length()) {
            val hour = hoursArray[i] as JSONObject
            val itemHour = WeatherModel(
                cityName = dayWeather.cityName,
                time = hour.getString("time"),
                condition = hour.getJSONObject("condition").getString("text"),
                imageUrlCondition = hour.getJSONObject("condition").getString("icon"),
                currentTemp = hour.getString("temp_c"),
                maxTemp = "",
                minTemp = "",
                hours = ""
            )
            list.add(itemHour)
        }
        val itemCurrent = WeatherModel(
            cityName = dayWeather.cityName,
            time = dayWeather.time,
            condition = dayWeather.condition,
            imageUrlCondition = dayWeather.imageUrlCondition,
            currentTemp = dayWeather.maxTemp + "/" + dayWeather.minTemp,
            maxTemp = "",
            minTemp = "",
            hours = ""
        )
        _liveDataCurrent.value = itemCurrent

        _liveDataListHour.value = list
    }

}