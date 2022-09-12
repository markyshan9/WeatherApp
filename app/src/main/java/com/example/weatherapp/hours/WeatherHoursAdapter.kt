package com.example.weatherapp.hours

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.databinding.ListItemBinding
import com.squareup.picasso.Picasso

class WeatherHoursAdapter : ListAdapter<HourWeather, WeatherHoursAdapter.Holder>(Comparator()) {

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ListItemBinding.bind(view)

        fun bind(item: HourWeather) = with(binding){
            tvDate.text = item.time
            tvCondition.text = item.condition
            (item.currentTemp + "°C").also { tvTemp.text = it }
            Picasso.get().load("https:" + item.imageUrlCondition).into(im)

        }
    }

    class Comparator : DiffUtil.ItemCallback<HourWeather>() {
        override fun areItemsTheSame(oldItem: HourWeather, newItem: HourWeather): Boolean {
//            надо сравнивать уникальные поля в элементах
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: HourWeather, newItem: HourWeather): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return Holder(view = view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))

    }


}