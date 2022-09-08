package com.example.weatherapp.days

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.MainViewModel
import com.example.weatherapp.adapters.WeatherModel
import com.example.weatherapp.databinding.FragmentDaysBinding

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
        mainViewModel.parseHoursByDay(dayWeather)
    }
}