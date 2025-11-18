package com.example.weedx

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class WeedLogsAdapter(private val logs: List<WeedLog>) :
    RecyclerView.Adapter<WeedLogsAdapter.WeedLogViewHolder>() {

    class WeedLogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val weedName: TextView = view.findViewById(R.id.weedName)
        val weedCount: TextView = view.findViewById(R.id.weedCount)
        val weedZone: TextView = view.findViewById(R.id.weedZone)
        val weedTime: TextView = view.findViewById(R.id.weedTime)
        val treatedBadge: CardView = view.findViewById(R.id.treatedBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeedLogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_weed_log, parent, false)
        return WeedLogViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeedLogViewHolder, position: Int) {
        val log = logs[position]
        
        holder.weedName.text = log.name
        holder.weedCount.text = "${log.weedsCount} weeds detected"
        holder.weedZone.text = "Zone ${log.zone}"
        holder.weedTime.text = log.timeAgo
        
        // Show/hide treated badge
        holder.treatedBadge.visibility = if (log.isTreated) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int = logs.size
}
