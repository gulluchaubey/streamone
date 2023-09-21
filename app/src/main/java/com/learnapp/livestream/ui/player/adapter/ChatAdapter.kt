package com.learnapp.livestream.ui.player.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.learnapp.livestream.ui.player.model.ChatMessage
import com.learnapp.livestream.ui.player.viewholder.UserChatViewHolder

class ChatAdapter(private val messages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<UserChatViewHolder>() {

    fun setNewMessages(messages: List<ChatMessage>) {
        this.messages.clear()
        this.messages.addAll(messages)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
        UserChatViewHolder {
        return UserChatViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: UserChatViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int {
        return messages.size
    }
}
