package com.example.weedx

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val messages: List<ChatMessage>) : 
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userMessageCard: CardView = view.findViewById(R.id.userMessageCard)
        val userMessageText: TextView = view.findViewById(R.id.userMessageText)
        val assistantMessageCard: CardView = view.findViewById(R.id.assistantMessageCard)
        val assistantMessageText: TextView = view.findViewById(R.id.assistantMessageText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        
        if (message.isUser) {
            // Show user message
            holder.userMessageCard.visibility = View.VISIBLE
            holder.userMessageText.text = message.text
            holder.assistantMessageCard.visibility = View.GONE
        } else {
            // Show assistant message
            holder.assistantMessageCard.visibility = View.VISIBLE
            holder.assistantMessageText.text = message.text
            holder.userMessageCard.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = messages.size
}
