package com.learnapp.livestream.data.models

import live.hms.video.media.tracks.HMSVideoTrack
import live.hms.video.sdk.models.HMSPeer

data class TrackPeerMap(
    val videoTrack: HMSVideoTrack?,
    val peer: HMSPeer
)
