package com.example.weatherapp.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.MainViewModel
import com.example.weatherapp.adapters.WeatherDaysAdapter
import com.example.weatherapp.adapters.WeatherHoursAdapter
import com.example.weatherapp.adapters.WeatherModel
import com.example.weatherapp.databinding.FragmentDaysBinding
import org.json.JSONArray
import org.json.JSONObject

class DaysFragment : Fragment(), WeatherDaysAdapter.Listener {
    private lateinit var binding: FragmentDaysBinding
    private val mainViewModel : MainViewModel by activityViewModels()
    private lateinit var adapter: WeatherDaysAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDaysBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRcView()

    }

    private fun initRcView() = with(binding) {
        rcView.layoutManager = LinearLayoutManager(activity)
        adapter = WeatherDaysAdapter(this@DaysFragment)
        rcView.adapter = adapter
        mainViewModel.liveDataListDay.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    companion object {

        @JvmStatic
        fun newInstance() = DaysFragment()
    }

    override fun onClick(dayWeather: WeatherModel) {
        Toast.makeText(context, dayWeather.time, Toast.LENGTH_SHORT).show()
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
            mainViewModel.liveDataCurrent.value = itemCurrent

        mainViewModel.liveDataListHour.value = list
    }
}