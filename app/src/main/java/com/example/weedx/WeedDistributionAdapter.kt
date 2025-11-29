package com.example.weedx

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

data class WeedDistributionItem(
    val weedType: String,
    val count: Int,
    val percentage: Float,
    val color: Int
)

class WeedDistributionAdapter : ListAdapter<WeedDistributionItem, WeedDistributionAdapter.DistributionViewHolder>(DistributionDiffCallback()) {

    companion object {
        // Color palette for weed types
        private val COLORS = listOf(
            Color.parseColor("#4CAF50"),  // Green
            Color.parseColor("#66BB6A"),  // Light Green
            Color.parseColor("#81C784"),  // Lighter Green
            Color.parseColor("#A5D6A7"),  // Even Lighter Green
            Color.parseColor("#C8E6C9"),  // Pale Green
            Color.parseColor("#E8F5E9"),  // Very Pale Green
            Color.parseColor("#8BC34A"),  // Lime
            Color.parseColor("#AED581"),  // Light Lime
        )

        fun createFromDistribution(distribution: Map<String, Int>): List<WeedDistributionItem> {
            val total = distribution.values.sum().toFloat()
            if (total == 0f) return emptyList()

            return distribution.entries
                .sortedByDescending { it.value }
                .mapIndexed { index, (weedType, count) ->
                    WeedDistributionItem(
                        weedType = weedType,
                        count = count,
                        percentage = (count / total) * 100,
                        color = COLORS[index % COLORS.size]
                    )
                }
        }
    }

    class DistributionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val colorIndicator: View = view.findViewById(R.id.colorIndicator)
        val weedTypeName: TextView = view.findViewById(R.id.weedTypeName)
        val weedPercentage: TextView = view.findViewById(R.id.weedPercentage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DistributionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_weed_distribution, parent, false)
        return DistributionViewHolder(view)
    }

    override fun onBindViewHolder(holder: DistributionViewHolder, position: Int) {
        val item = getItem(position)
        
        // Set color indicator
        val drawable = holder.colorIndicator.background as? GradientDrawable
        drawable?.setColor(item.color)
        
        holder.weedTypeName.text = item.weedType
        holder.weedPercentage.text = String.format("%.1f%% (%d)", item.percentage, item.count)
    }

    class DistributionDiffCallback : DiffUtil.ItemCallback<WeedDistributionItem>() {
        override fun areItemsTheSame(oldItem: WeedDistributionItem, newItem: WeedDistributionItem): Boolean {
            return oldItem.weedType == newItem.weedType
        }

        override fun areContentsTheSame(oldItem: WeedDistributionItem, newItem: WeedDistributionItem): Boolean {
            return oldItem == newItem
        }
    }
}
