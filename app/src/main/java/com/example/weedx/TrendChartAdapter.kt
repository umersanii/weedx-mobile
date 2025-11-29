package com.example.weedx

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weedx.data.models.response.TrendData
import java.text.SimpleDateFormat
import java.util.Locale

class TrendChartAdapter : ListAdapter<TrendData, TrendChartAdapter.TrendViewHolder>(TrendDiffCallback()) {

    private var maxValue: Int = 100

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
        val item = getItem(position)
        
        // Format date to day name (e.g., "Mon", "Tue")
        holder.dayLabel.text = formatDateToDay(item.date)
        
        // Calculate progress percentage based on max value
        val progress = if (maxValue > 0) ((item.count.toFloat() / maxValue) * 100).toInt() else 0
        holder.progressBar.progress = progress.coerceIn(0, 100)
        
        holder.valueLabel.text = item.count.toString()
    }

    fun submitListWithMax(list: List<TrendData>) {
        maxValue = list.maxOfOrNull { it.count } ?: 100
        submitList(list)
    }

    private fun formatDateToDay(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("EEE", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString.takeLast(2)
        } catch (e: Exception) {
            // Fallback: return last 2 characters (day number)
            dateString.takeLast(2)
        }
    }

    class TrendDiffCallback : DiffUtil.ItemCallback<TrendData>() {
        override fun areItemsTheSame(oldItem: TrendData, newItem: TrendData): Boolean {
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: TrendData, newItem: TrendData): Boolean {
            return oldItem == newItem
        }
    }
}
