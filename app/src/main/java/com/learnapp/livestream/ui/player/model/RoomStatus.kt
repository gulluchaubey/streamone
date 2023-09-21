package com.learnapp.livestream.ui.player.model

enum class RoomStatus(val message: String, val isSticky: Boolean) {
    RECONNECTED("Reconnected", false),
    RECONNECTING("Trying to reconnect, please check your internet connection.", true),
    TRACK_DEGRADED("Video quality degraded due to poor network", true),
    TRACK_RESTORED("Video track restored", false)
}
