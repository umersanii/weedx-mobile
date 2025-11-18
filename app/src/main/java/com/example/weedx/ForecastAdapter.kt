package com.example.weedx

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ForecastAdapter(private val forecasts: List<ForecastDay>) :
    RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    class ForecastViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayText: TextView = view.findViewById(R.id.dayText)
        val forecastIcon: ImageView = view.findViewById(R.id.forecastIcon)
        val forecastDescription: TextView = view.findViewById(R.id.forecastDescription)
        val maxTemp: TextView = view.findViewById(R.id.maxTemp)
        val minTemp: TextView = view.findViewById(R.id.minTemp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_forecast, parent, false)
        return ForecastViewHolder(view)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val forecast = forecasts[position]
        holder.dayText.text = forecast.day
        holder.forecastIcon.setImageResource(forecast.icon)
        holder.forecastDescription.text = forecast.description
        holder.maxTemp.text = "${forecast.maxTemp}°"
        holder.minTemp.text = "${forecast.minTemp}°"
    }

    override fun getItemCount() = forecasts.size
}
