package com.example.weedx

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val messages: List<ChatMessage>) : 
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_USER = 0
        private const val VIEW_TYPE_ASSISTANT = 1
        private const val VIEW_TYPE_TYPING = 2
    }

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userMessageCard: CardView = view.findViewById(R.id.userMessageCard)
        val userMessageText: TextView = view.findViewById(R.id.userMessageText)
        val assistantMessageCard: CardView = view.findViewById(R.id.assistantMessageCard)
        val assistantMessageText: TextView = view.findViewById(R.id.assistantMessageText)
    }

    class TypingViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return when {
            message.isTyping -> VIEW_TYPE_TYPING
            message.isUser -> VIEW_TYPE_USER
            else -> VIEW_TYPE_ASSISTANT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_TYPING -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_typing, parent, false)
                TypingViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_message, parent, false)
                ChatViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        
        when (holder) {
            is ChatViewHolder -> {
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
            is TypingViewHolder -> {
                // Typing indicator view is already set up in the layout
            }
        }
    }

    override fun getItemCount(): Int = messages.size
}
