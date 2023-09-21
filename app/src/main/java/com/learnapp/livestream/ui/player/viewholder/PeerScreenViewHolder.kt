package com.learnapp.livestream.ui.player.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutParams
import com.learnapp.livestream.R
import com.learnapp.livestream.data.models.TrackPeerMap
import com.learnapp.livestream.databinding.ListItemPeerScreenBinding
import live.hms.video.utils.SharedEglContext
import org.webrtc.RendererCommon
import timber.log.Timber

class PeerScreenViewHolder(
    private val binding: ListItemPeerScreenBinding,
    private val getItem: (Int) -> TrackPeerMap
) : RecyclerView.ViewHolder(binding.root) {

    private var sinkAdded = false

    init {
        binding.videoSurfaceView.apply {
            Timber.tag(TAG).v("init scale aspect ratio")
            setEnableHardwareScaler(true)
            setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
        }
    }

    private fun resizePlayer(peerCount: Int, peer: TrackPeerMap) {
        Timber.tag(TAG).v("resizePlayer peerCount - $peerCount")
        if (peerCount == PEER_COUNT_ONE) {
            val params = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            val dimen = binding.root.context.resources.getDimensionPixelSize(R.dimen.size_0)
            params.setMargins(dimen)
            binding.playerLayout.layoutParams = params
            binding.playerLayout.setPadding(dimen)
            binding.playerLayout.background = null
            binding.peerName.isVisible = false
        } else if (peerCount >= PEER_COUNT_TWO) {
            val params = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            val margin = binding.root.context.resources.getDimensionPixelSize(R.dimen.size_8)
            params.setMargins(margin)

            if (peerCount > PEER_COUNT_TWO) {
                val originalHeight = binding.root.context.resources.displayMetrics.heightPixels
                params.height = (originalHeight - (margin * MARGIN_MULTIPLIER)) / SCREEN_HEIGHT_DIVIDER
            }
            binding.playerLayout.layoutParams = params
            val padding = binding.root.context.resources.getDimensionPixelSize(R.dimen.size_4)
            binding.playerLayout.setPadding(padding)

            val background = if (peer.peer.audioTrack?.isMute == false) {
                AppCompatResources.getDrawable(binding.root.context, R.drawable.bg_screen_tile_active)
            } else {
                AppCompatResources.getDrawable(binding.root.context, R.drawable.bg_screen_tile)
            }
            binding.playerLayout.background = background
            binding.peerName.isVisible = true
        }
    }

    fun startSurfaceView() {
        Timber.tag(TAG).v("startSurfaceView() $adapterPosition")
        if (!sinkAdded) {
            binding.videoSurfaceView.apply {
                getItem(adapterPosition).videoTrack?.let { hmsVideoTrack ->
                    Timber.tag(TAG).v("startSurfaceView() ${getItem(adapterPosition).videoTrack}")
                    init(SharedEglContext.context, null)
                    hmsVideoTrack.addSink(this)
                    sinkAdded = true
                }
            }
        }
    }

    fun stopSurfaceView() {
        Timber.tag(TAG).v("stopSurfaceView() $adapterPosition")
        binding.videoSurfaceView.apply {
            if (sinkAdded && adapterPosition != NEGATIVE_ADAPTER_POSITION) {
                getItem(adapterPosition).videoTrack?.let {
                    Timber.tag(TAG).v("stopSurfaceView() ${getItem(adapterPosition).videoTrack}")
                    it.removeSink(this)
                    release()
                    sinkAdded = false
                }
            }
        }
    }

    fun bind(peer: TrackPeerMap, itemCount: Int) {
        Timber.tag(TAG).v("bind")
        resizePlayer(itemCount, peer)
        if (!sinkAdded) {
            binding.videoSurfaceView.apply {
                Timber.tag(TAG).v("bind apply")
                setEnableHardwareScaler(true)
                setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
                sinkAdded = false
            }
        }
        binding.peerName.text = peer.peer.name
    }

    companion object {
        private const val TAG = "PeerScreenViewHolder"
        private const val PEER_COUNT_ONE = 1
        private const val PEER_COUNT_TWO = 2
        private const val SCREEN_HEIGHT_DIVIDER = 2
        private const val MARGIN_MULTIPLIER = 6
        private const val NEGATIVE_ADAPTER_POSITION = -1

        fun from(parent: ViewGroup, getItem: (Int) -> TrackPeerMap): PeerScreenViewHolder {
            Timber.tag(TAG).v("from")
            return PeerScreenViewHolder(
                ListItemPeerScreenBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ),
                    parent,
                    false
                ),
                getItem
            )
        }
    }
}
