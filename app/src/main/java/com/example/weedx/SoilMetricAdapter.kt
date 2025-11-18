package com.example.weedx

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SoilMetricAdapter(private val metrics: List<SoilMetric>) :
    RecyclerView.Adapter<SoilMetricAdapter.MetricViewHolder>() {

    class MetricViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val metricIcon: ImageView = view.findViewById(R.id.metricIcon)
        val metricName: TextView = view.findViewById(R.id.metricName)
        val metricValue: TextView = view.findViewById(R.id.metricValue)
        val metricStatus: TextView = view.findViewById(R.id.metricStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MetricViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_soil_metric, parent, false)
        return MetricViewHolder(view)
    }

    override fun onBindViewHolder(holder: MetricViewHolder, position: Int) {
        val metric = metrics[position]
        holder.metricIcon.setImageResource(metric.icon)
        holder.metricName.text = metric.name
        holder.metricValue.text = metric.value
        
        if (metric.status != null) {
            holder.metricStatus.visibility = View.VISIBLE
            holder.metricStatus.text = metric.status
        } else {
            holder.metricStatus.visibility = View.GONE
        }
    }

    override fun getItemCount() = metrics.size
}
