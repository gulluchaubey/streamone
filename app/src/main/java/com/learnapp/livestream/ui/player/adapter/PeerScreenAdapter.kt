package com.learnapp.livestream.ui.player.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.learnapp.livestream.data.models.TrackPeerMap
import com.learnapp.livestream.ui.player.viewholder.PeerScreenViewHolder
import timber.log.Timber

class PeerScreenAdapter : ListAdapter<TrackPeerMap, PeerScreenViewHolder>(PeerDiffCallback()) {

    companion object {
        private const val TAG = "PeerScreenAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeerScreenViewHolder {
        return PeerScreenViewHolder.from(parent, ::getItem)
    }

    override fun onBindViewHolder(holder: PeerScreenViewHolder, position: Int) {
        getItem(position)?.let {
            holder.stopSurfaceView()
            Timber.tag(TAG).v("onBindViewHolder $position")
            holder.bind(it, itemCount)
        }
    }

    override fun onViewAttachedToWindow(holder: PeerScreenViewHolder) {
        super.onViewAttachedToWindow(holder)
        Timber.tag(TAG).v("onViewAttachedToWindow")
        holder.startSurfaceView()
    }

    override fun onViewDetachedFromWindow(holder: PeerScreenViewHolder) {
        super.onViewDetachedFromWindow(holder)
        Timber.tag(TAG).v("onViewDetachedFromWindow")
        holder.stopSurfaceView()
    }
}
