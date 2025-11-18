package com.example.weedx

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WeedDistributionAdapter(private val distributions: List<WeedDistribution>) :
    RecyclerView.Adapter<WeedDistributionAdapter.DistributionViewHolder>() {

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
        val item = distributions[position]
        
        // Set color indicator
        val drawable = holder.colorIndicator.background as? GradientDrawable
        drawable?.setColor(item.color)
        
        holder.weedTypeName.text = item.weedType
        holder.weedPercentage.text = "${item.percentage}%"
    }

    override fun getItemCount() = distributions.size
}
