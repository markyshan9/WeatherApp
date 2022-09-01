package com.example.weatherapp.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherapp.MainViewModel
import com.example.weatherapp.adapters.VpAdapter
import com.example.weatherapp.adapters.WeatherModel
import com.example.weatherapp.databinding.FragmentMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONObject


const val API_KEY = "e9d5b07b591240cfa2b132641220109"

class MainFragment : Fragment() {
    private val fList: List<Fragment> = listOf(
        HoursFragment.newInstance(),
        DaysFragment.newInstance()
    )
    private val tList = listOf(
        "hours",
        "days"
    )
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var pLauncher: ActivityResultLauncher<String>
    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()
        init()
        getLocation(requireContext(), false)
        setWeatherCurrent()
    }

    private fun init() = with(binding) {
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        val adapter = VpAdapter(activity as FragmentActivity, fList)
        vp.adapter = adapter
        TabLayoutMediator(tabLayout, vp) { tab, pos ->
            tab.text = tList[pos]
        }.attach()
        ibSync.setOnClickListener {
            tabLayout.selectTab(tabLayout.getTabAt(0))
            getLocation(requireContext(), true)
        }
        ibSearch.setOnClickListener {
            if (etSearch.visibility == View.GONE) {
                etSearch.visibility = View.VISIBLE
                bSearch.visibility = View.VISIBLE
            } else {
                etSearch.visibility = View.GONE
                bSearch.visibility = View.GONE
            }
        }
        bSearch.setOnClickListener {
            searchCity()
            binding.etSearch.text = null
        }
    }

    private fun getLocation(context: Context, reset: Boolean) {
        val ct = CancellationTokenSource()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, ct.token)
            .addOnCompleteListener {
                requestWeatherData("${it.result.latitude},${it.result.longitude}", reset, context)
            }
    }

    private fun setWeatherCurrent() = with(binding) {
        mainViewModel.liveDataCurrent.observe(viewLifecycleOwner) { it ->
            tvData.text = it.time
            tvCondition.text = it.condition
            tvCityName.text = it.cityName
            tvCurrentTemp.text = "${it.currentTemp}°C"
            Picasso.get().load("https:" + it.imageUrlCondition).into(imWeather)
            if (it.maxTemp == it.minTemp) {
                tvMaxMin.text = ""
            } else {
                ("${it.maxTemp}°C / ${it.minTemp}°C").also { tvMaxMin.text = it }
            }
        }
    }

    private fun permissionListener() {
        pLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {

        }
    }

    private fun checkPermission() {
        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionListener()
            pLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }


    private fun requestWeatherData(cityName: String, reset: Boolean, context: Context) = with(binding) {
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
                etSearch.visibility = View.GONE
                bSearch.visibility = View.GONE
            },
            { error ->
                Toast.makeText(context, "Please enter a valid city name", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(request)
    }




    private fun parseWeatherData(result: String, reset: Boolean) {
        val mainObject = JSONObject(result)
        val listDays: List<WeatherModel> = parseDays(mainObject = mainObject)
        val currentWeather = parseCurrentData(mainObject = mainObject, weatherItem = listDays[0])
        val listHours = parseHours(currentWeather.hours)
        if (mainViewModel.liveDataListDay.value == null || reset)
            mainViewModel.liveDataListDay.value = listDays.subList(1, listDays.size)
        if (mainViewModel.liveDataCurrent.value == null || reset)
            mainViewModel.liveDataCurrent.value = currentWeather
        if (mainViewModel.liveDataListHour.value == null || reset)
            mainViewModel.liveDataListHour.value = listHours

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
                maxTemp = day.getJSONObject("day").getString("maxtemp_c").toFloat().toInt().toString(),
                minTemp = day.getJSONObject("day").getString("mintemp_c").toFloat().toInt().toString(),
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
                maxTemp = "",
                minTemp = "",
                hours = ""
            )
            list.add(itemHour)
        }
        return list
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()

    }

    private fun searchCity() = with(binding) {
        val cityName: String = etSearch.text.toString()
        requestWeatherData(cityName = cityName, true, requireContext())

    }


}