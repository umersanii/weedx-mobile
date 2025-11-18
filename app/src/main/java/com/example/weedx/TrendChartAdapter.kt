package com.example.weedx

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TrendChartAdapter(private val trendData: List<TrendData>) :
    RecyclerView.Adapter<TrendChartAdapter.TrendViewHolder>() {

    class TrendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayLabel: TextView = view.findViewById(R.id.dayLabel)
        val progressBar: ProgressBar = view.findViewById(R.id.trendProgressBar)
        val valueLabel: TextView = view.findViewById(R.id.valueLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trend_chart, parent, false)
        return TrendViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrendViewHolder, position: Int) {
        val item = trendData[position]
        holder.dayLabel.text = item.day
        holder.progressBar.progress = item.progress
        holder.valueLabel.text = item.value.toString()
    }

    override fun getItemCount() = trendData.size
}
