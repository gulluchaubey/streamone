package com.learnapp.livestream.ui.player.adapter

import androidx.recyclerview.widget.DiffUtil
import com.learnapp.livestream.data.models.TrackPeerMap

class PeerDiffCallback : DiffUtil.ItemCallback<TrackPeerMap>() {

    companion object {
        private const val TAG = "PeerDiffCallback"
    }

    override fun areItemsTheSame(
        oldItem: TrackPeerMap,
        newItem: TrackPeerMap
    ): Boolean {
        return oldItem.peer.peerID == newItem.peer.peerID &&
            oldItem.videoTrack?.trackId == newItem.videoTrack?.trackId
    }

    override fun areContentsTheSame(
        oldItem: TrackPeerMap,
        newItem: TrackPeerMap
    ): Boolean {
        return oldItem.peer.audioTrack?.isMute == newItem.peer.audioTrack?.isMute
    }
}
