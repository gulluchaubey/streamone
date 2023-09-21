package com.learnapp.livestream.ui.player.model

import com.learnapp.livestream.utils.getObjectFromJson
import live.hms.video.sdk.models.HMSMessage

data class ChatMessage(
    val senderName: String?,
    val time: Long,
    val message: String,
    val isSentByMe: Boolean,
    val avatarUrl: String?
) {

    constructor(message: HMSMessage, sentByMe: Boolean) : this(
        message.sender?.name,
        message.serverReceiveTime,
        message.message,
        sentByMe,
        getObjectFromJson(
            UserMetaData::class.java,
            message.sender?.metadata
        )?.avatarUrl
    )
}
