package com.example.weatherapp.days

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.databinding.ListItemBinding
import com.squareup.picasso.Picasso

class WeatherDaysAdapter(val listener: Listener) : ListAdapter<DayWeather, WeatherDaysAdapter.Holder>(
    Comparator()
) {

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ListItemBinding.bind(view)

        fun bind(item: DayWeather, listener: Listener) = with(binding){
            tvDate.text = item.time
            tvCondition.text = item.condition
            (item.maxTemp + "°C / " + item.minTemp + "°C").also { tvTemp.text = it }
            Picasso.get().load("https:" + item.imageUrlCondition).into(im)

            itemView.setOnClickListener {
                listener.onClick(item)
            }
        }
    }

    class Comparator : DiffUtil.ItemCallback<DayWeather>() {
        override fun areItemsTheSame(oldItem: DayWeather, newItem: DayWeather): Boolean {
//            надо сравнивать уникальные поля в элементах
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: DayWeather, newItem: DayWeather): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return Holder(view = view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position), listener)

    }

    interface Listener {
        fun onClick(dayWeather: DayWeather)
    }


}