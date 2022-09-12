package com.example.weatherapp.current

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.example.weatherapp.DialogManager
import com.example.weatherapp.MainViewModel
import com.example.weatherapp.databinding.FragmentMainBinding
import com.example.weatherapp.days.DaysFragment
import com.example.weatherapp.hours.HoursFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso


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

    override fun onResume() {
        super.onResume()
        checkLocation(requireContext(), false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()
        init()
        setWeatherCurrent()
        checkSearchSuccess()
    }

    private fun init() = with(binding) {
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        val adapter = ViewPagerAdapter(activity as FragmentActivity, fList)
        vp.adapter = adapter
        TabLayoutMediator(tabLayout, vp) { tab, pos ->
            tab.text = tList[pos]
        }.attach()
        ibSync.setOnClickListener {
            tabLayout.selectTab(tabLayout.getTabAt(0))
            checkLocation(requireContext(), true)
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
        }
    }

    private fun checkLocation(context: Context, reset: Boolean) {
        if(isLocationEnabled()) {
            getLocation(context, reset)
        } else {
            DialogManager.locationSettingsDialog(context, object : DialogManager.Listener{
                override fun onClick() {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            })
        }
    }

    private fun isLocationEnabled() : Boolean{
        val lm = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun getLocation(context: Context, reset: Boolean) {
            val ct = CancellationTokenSource()
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, ct.token)
                .addOnCompleteListener {
                    mainViewModel.requestWeatherData(
                        "${it.result.latitude},${it.result.longitude}",
                        reset,
                        context
                    )
                }
    }

    private fun setWeatherCurrent() = with(binding) {
        mainViewModel.liveDataCurrent.observe(viewLifecycleOwner) {
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



    private fun checkPermission() {
        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            pLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }




    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()

    }

    private fun searchCity() = with(binding) {
        val cityName: String = etSearch.text.toString()
        mainViewModel.requestWeatherData(cityName = cityName, true, requireContext())
    }

    private fun checkSearchSuccess() = with(binding){
        mainViewModel.liveDataWeatherBoolean.observe(viewLifecycleOwner){
            if (it){
                binding.etSearch.text = null
                etSearch.visibility = View.GONE
                bSearch.visibility = View.GONE
            } else {
                etSearch.visibility = View.VISIBLE
                bSearch.visibility = View.VISIBLE
            }
        }
    }


}