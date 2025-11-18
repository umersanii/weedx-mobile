package com.example.weedx

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TimelineAdapter(private val events: List<TimelineEvent>) :
    RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder>() {

    class TimelineViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timelineDot: View = view.findViewById(R.id.timelineDot)
        val timelineLine: View = view.findViewById(R.id.timelineLine)
        val timelineTitle: TextView = view.findViewById(R.id.timelineTitle)
        val timelineDescription: TextView = view.findViewById(R.id.timelineDescription)
        val timelineTime: TextView = view.findViewById(R.id.timelineTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_timeline, parent, false)
        return TimelineViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        val event = events[position]
        
        holder.timelineTitle.text = event.title
        holder.timelineDescription.text = event.description
        holder.timelineTime.text = event.timeAgo
        
        // Hide line for last item
        if (event.isLast) {
            holder.timelineLine.visibility = View.INVISIBLE
        } else {
            holder.timelineLine.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int = events.size
}
