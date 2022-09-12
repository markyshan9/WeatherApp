package com.example.weatherapp

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherapp.days.DayWeather
import com.example.weatherapp.hours.HourWeather
import com.example.weatherapp.current.WeatherModel
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

class MainViewModel : ViewModel() {



    private val _liveDataCurrent = MutableLiveData<WeatherModel>()
    var liveDataCurrent: LiveData<WeatherModel> = _liveDataCurrent

    private val _liveDataListDay = MutableLiveData<List<DayWeather>>()
    var liveDataListDay: LiveData<List<DayWeather>> = _liveDataListDay

    private val _liveDataListHour = MutableLiveData<List<HourWeather>>()
    var liveDataListHour: LiveData<List<HourWeather>> = _liveDataListHour

    private val _liveDataWeatherBoolean = MutableLiveData<Boolean>()
    var liveDataWeatherBoolean: LiveData<Boolean> = _liveDataWeatherBoolean


    fun requestWeatherData(cityName: String, reset: Boolean, context: Context) {
        val url = "https://api.weatherapi.com/v1/forecast.json?" +
                "key=" +
                BuildConfig.API_KEY +
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
        val listDays: List<DayWeather> = parseDays(mainObject = mainObject)
        val currentWeather = parseCurrentData(mainObject = mainObject, weatherItem = listDays[0])
        val listHours = parseHours(currentWeather.hours)
        if (_liveDataListDay.value == null || reset)
            _liveDataListDay.value = listDays.subList(1, listDays.size)
        if (_liveDataCurrent.value == null || reset)
            _liveDataCurrent.value = currentWeather
        if (_liveDataListHour.value == null || reset)
            _liveDataListHour.value = listHours

    }


    private fun parseCurrentData(mainObject: JSONObject, weatherItem: DayWeather): WeatherModel {
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

    private fun parseDays(mainObject: JSONObject): List<DayWeather> {
        val list = ArrayList<DayWeather>()
        val daysArray = mainObject.getJSONObject("forecast")
            .getJSONArray("forecastday")
        val name = mainObject.getJSONObject("location").getString("name")

        for (i in 0 until daysArray.length()) {
            val day = daysArray[i] as JSONObject
            val itemDay = DayWeather(
                cityName = name,
                time = day.getString("date"),
                condition = day.getJSONObject("day").getJSONObject("condition")
                    .getString("text"),
                imageUrlCondition = day.getJSONObject("day").getJSONObject("condition")
                    .getString("icon"),
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


    private fun parseHours(hours: String): List<HourWeather> {
        val list = ArrayList<HourWeather>()
        val hoursArray = JSONArray(hours)
        for (i in 0 until hoursArray.length()) {
            val hour = hoursArray[i] as JSONObject
            val itemHour = HourWeather(
                cityName = "",
                time = hour.getString("time"),
                condition = hour.getJSONObject("condition").getString("text"),
                imageUrlCondition = hour.getJSONObject("condition").getString("icon"),
                currentTemp = hour.getString("temp_c")
            )
            list.add(itemHour)
        }
        return list
    }

    fun parseHoursByDay(dayWeather: DayWeather) {
        val list = ArrayList<HourWeather>()
        val hoursArray = JSONArray(dayWeather.hours)
        for (i in 0 until hoursArray.length()) {
            val hour = hoursArray[i] as JSONObject
            val itemHour = HourWeather(
                cityName = dayWeather.cityName,
                time = hour.getString("time"),
                condition = hour.getJSONObject("condition").getString("text"),
                imageUrlCondition = hour.getJSONObject("condition").getString("icon"),
                currentTemp = hour.getString("temp_c")
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