package com.learnapp.livestream.ui.player.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.learnapp.livestream.databinding.ListItemUserChatBinding
import com.learnapp.livestream.ui.player.model.ChatMessage
import com.learnapp.livestream.utils.Constants
import com.learnapp.livestream.utils.DateUtils
import com.learnapp.livestream.utils.loadImageFromUrl

class UserChatViewHolder(private val binding: ListItemUserChatBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(message: ChatMessage) {
        binding.userNameTextView.text = if (message.isSentByMe) {
            DEFAULT_NAME
        } else if (!message.senderName.isNullOrEmpty()) {
            message.senderName
        } else {
            Constants.UserMetaData.DEFAULT_USER_NAME
        }
        binding.messageTextView.text = message.message
        binding.timeTextView.text = DateUtils.getMessageDate(
            message.time,
            binding.timeTextView.context
        )
        binding.liveUserImageView.loadImageFromUrl(message.avatarUrl)
    }

    companion object {

        private const val DEFAULT_NAME = "You"

        fun from(parent: ViewGroup): UserChatViewHolder {
            return UserChatViewHolder(
                ListItemUserChatBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }
}
