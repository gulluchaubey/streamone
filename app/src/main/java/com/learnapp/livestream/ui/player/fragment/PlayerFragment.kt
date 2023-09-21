package com.learnapp.livestream.ui.player.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.learnapp.livestream.R
import com.learnapp.livestream.data.models.TrackPeerMap
import com.learnapp.livestream.data.network.remote.Resource
import com.learnapp.livestream.databinding.FragmentPlayerBinding
import com.learnapp.livestream.ui.base.BaseFragment
import com.learnapp.livestream.ui.player.adapter.PeerScreenAdapter
import com.learnapp.livestream.ui.player.model.ApiErrorCodes
import com.learnapp.livestream.ui.player.model.NetworkQualityCode
import com.learnapp.livestream.ui.player.model.RoomStatus
import com.learnapp.livestream.ui.player.viewmodel.PlayerViewModel
import com.learnapp.livestream.utils.handleApiError
import com.learnapp.livestream.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class PlayerFragment : BaseFragment(), View.OnClickListener {

    companion object {
        private const val TAG = "PlayerFragment"
        private const val CONNECTION_ONLINE_DELAY = 2000L
        private const val ROOM_STATUS_DELAY = 1000L
        private const val DEFAULT_SPAN_COUNT = 1
        private const val MAX_SPAN_COUNT = 2
        private const val UN_READ_MESSAGE_COUNT_THRESHOLD = 9
        private const val DEFAULT_UN_READ_COUNT = 0
    }

    private val viewModel: PlayerViewModel by activityViewModels()

    private var _fragmentBinding: FragmentPlayerBinding? = null
    private val fragmentBinding get() = _fragmentBinding!!

    private var peerAdapter: PeerScreenAdapter? = null
    private var gridLayoutManager: GridLayoutManager? = null

    private var peerCount: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _fragmentBinding = FragmentPlayerBinding.inflate(
            inflater,
            container,
            false
        )
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.tag(TAG).v("onViewCreated()")
        setBaseViewModel(viewModel)
        initViewVisibility()
        setOnClickListener()
        setObserver()
        checkLiveStream()
        initPeerAdapter()
        toggleSystemBars()
        //        showPeerCount(viewModel.peerCount.value)
    }

    private fun initViewVisibility() {
        fragmentBinding.viewGroup.visible(false)
        fragmentBinding.connectivityTextView.visible(false)
        fragmentBinding.statStreamTextView.visible(false)
        setUnReadMessageCount(viewModel.unreadMessagesCount.value ?: DEFAULT_UN_READ_COUNT)
    }

    private fun checkLiveStream() {
        try {
            viewModel.initLiveStream(requireActivity().application)
        } catch (exception: IllegalStateException) {
            Timber.tag(TAG).e("$exception")
        }
    }

    override fun onDestroy() {
        Timber.tag(TAG).v("onDestroy")
        viewModel.getHmsSdk()?.leave()
        super.onDestroy()
    }

    private fun setOnClickListener() {
        fragmentBinding.userCountLayout.setOnClickListener(this)
    }

    private fun setObserver() {
        viewModel.icChatWindowHidden.observe(viewLifecycleOwner) {
            it?.let {
                onUserCountClick()
                viewModel.closeChatWindow(null)
            }
        }
        viewModel.unreadMessagesCount.observe(viewLifecycleOwner) { setUnReadMessageCount(it) }
        viewModel.isRoomEnded.observe(viewLifecycleOwner) { endRoom(it) }
        viewModel.roomStatus.observe(viewLifecycleOwner) { setRoomStatus(it) }
        viewModel.isJoined.observe(viewLifecycleOwner) { onRoomJoined(it) }
        viewModel.errorCode.observe(viewLifecycleOwner) { showErrors(it) }
        viewModel.resourceStatus.observe(viewLifecycleOwner) { setResourceStatus(it) }
        viewModel.peerList.observe(viewLifecycleOwner) { onPeerListReceived(it) }
        viewModel.networkQualityCode.observe(viewLifecycleOwner) { showNetworkStatus(it) }

        //        viewModel.peerCount.observe(viewLifecycleOwner) { showPeerCount(it) }
    }

    private fun onPeerListReceived(peerList: List<TrackPeerMap>) {
        Timber.tag(TAG).v("onPeerListReceived ${peerList.size}")
        if (peerList.isNotEmpty()) {
            fragmentBinding.statStreamTextView.visible(false)
        }
        val count = if (peerList.isEmpty()) {
            DEFAULT_SPAN_COUNT
        } else if (peerList.size <= MAX_SPAN_COUNT) {
            peerList.size
        } else {
            MAX_SPAN_COUNT
        }
        gridLayoutManager?.spanCount = count
        fragmentBinding.recyclerView.apply {
            layoutManager = gridLayoutManager
        }
        val runnable = Runnable {
            Timber.tag(TAG).v("Runnable")
            if (peerCount != peerList.size) {
                Timber.tag(TAG).v("Runnable count has changed")
                peerCount = peerList.size
                peerAdapter?.notifyDataSetChanged()
            }
        }
        peerAdapter?.submitList(peerList, runnable)
    }

    private fun onRoomJoined(isRoomJoined: Boolean?) {
        isRoomJoined?.let {
            fragmentBinding.userCountLayout.visible(it)
            fragmentBinding.statStreamTextView.visible(true)

            fragmentBinding.connectivityTextView.text = getString(R.string.workshop_joined)
            fragmentBinding.connectivityTextView.backgroundTintList =
                resources.getColorStateList(R.color.lime_green, null)
            fragmentBinding.connectivityTextView.setTextColor(
                resources.getColorStateList(R.color.white, null)
            )
            fragmentBinding.connectivityTextView.visible(true)
            fragmentBinding.chatFragmentContainerView.visible(true)

            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                fragmentBinding.connectivityTextView.visible(false)
            }, CONNECTION_ONLINE_DELAY)

            viewModel.clearJoinFlag()
        }
    }

    /**
     * if count is 3 digit then show 99+ and
     * width / height - 22dp
     * if count is 1 digit then
     * width / height - 20dp
     */
    private fun setUnReadMessageCount(count: Int) {
        Timber.tag(TAG).v("setUnReadMessageCount($count)")
        try {
            val dimen = if (count > UN_READ_MESSAGE_COUNT_THRESHOLD) {
                R.dimen.size_22
            } else {
                R.dimen.size_18
            }
            val size = requireContext().resources.getDimensionPixelSize(dimen)
            Timber.tag(TAG).v("setUnReadMessageCount $size")
            fragmentBinding.countTextView.width = size
            fragmentBinding.countTextView.height = size
        } catch (exception: IllegalStateException) {
            Timber.tag(TAG).e("setUnReadMessageCount $exception")
        }
        fragmentBinding.countTextView.text = "$count"
    }

    private fun endRoom(isRoomEnded: Boolean) {
        Timber.tag(TAG).v("endRoom() $isRoomEnded")
        try {
            requireActivity().finish()
        } catch (exception: IllegalStateException) {
            Timber.tag(TAG).v("endRoom() $exception")
        }
    }

    override fun onClick(view: View) {
        if (view.id == fragmentBinding.userCountLayout.id) {
            onUserCountClick()
        }
    }

    private fun onUserCountClick() {
        viewModel.clearMessageReceivedCount()
        fragmentBinding.chatFragmentContainerView.visible(
            !fragmentBinding.chatFragmentContainerView.isVisible
        )
    }

    private fun showNetworkStatus(networkQualityCode: NetworkQualityCode) {
        fragmentBinding.networkStatusImageView.setImageResource(networkQualityCode.resourceId)
        fragmentBinding.networkStatusImageView.isVisible = true
    }

    private fun setRoomStatus(roomStatus: RoomStatus) {
        Timber.tag(TAG).v("setRoomStatus($roomStatus)")
        if (!roomStatus.isSticky) {
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                fragmentBinding.connectivityTextView.visible(false)
            }, ROOM_STATUS_DELAY)
        }
        fragmentBinding.connectivityTextView.text = roomStatus.message
        fragmentBinding.connectivityTextView.backgroundTintList =
            resources.getColorStateList(R.color.raisin_black, null)
        fragmentBinding.connectivityTextView.setTextColor(
            resources.getColorStateList(R.color.white, null)
        )
        fragmentBinding.connectivityTextView.visible(true)
    }

    private fun showErrors(apiErrorCodes: ApiErrorCodes?) {
        Timber.tag(TAG).e("showErrors $apiErrorCodes")
        apiErrorCodes?.let { error ->
            fragmentBinding.connectivityTextView.backgroundTintList =
                resources.getColorStateList(R.color.raisin_black, null)
            fragmentBinding.connectivityTextView.setTextColor(
                resources.getColorStateList(R.color.white, null)
            )
            fragmentBinding.connectivityTextView.text = error.description
            fragmentBinding.connectivityTextView.visible(true)
        }
    }

    private fun setResourceStatus(resourceStatus: Resource<Any>?) {
        when (resourceStatus) {
            is Resource.Success -> {
                Timber.tag(TAG).v("setResourceStatus() Resource Success")
                dismissProgressDialog(true)
            }
            is Resource.Loading -> {
                Timber.tag(TAG).v("setResourceStatus() Resource Loading")
                resourceStatus.status?.let {
                    showProgressDialog(it, TAG, true)
                }
            }
            is Resource.Error -> {
                Timber.tag(TAG).v("setResourceStatus() Resource Error")
                dismissProgressDialog(true)
                handleApiError(resourceStatus)
            }
            else -> {
                Timber.tag(TAG).v("setResourceStatus() No Resource")
            }
        }
    }

    private fun initPeerAdapter() {
        try {
            peerAdapter = PeerScreenAdapter()
            gridLayoutManager = GridLayoutManager(requireActivity(), DEFAULT_SPAN_COUNT)
            fragmentBinding.recyclerView.apply {
                layoutManager = gridLayoutManager
                adapter = peerAdapter
            }
        } catch (exception: IllegalStateException) {
            Timber.tag(TAG).e("initPeerAdapter $exception")
        }
    }

    private fun toggleSystemBars() {
        try {
            val windowInsetsController =
                ViewCompat.getWindowInsetsController(requireActivity().window.decorView) ?: return
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        } catch (exception: IllegalStateException) {
            Timber.tag(TAG).e("toggleSystemBars $exception")
        }
    }

    //    private fun showPeerCount(peerCount: Int?) {
//        peerCount?.let {
//            fragmentBinding.countTextView.text = "$it"
//        }
//    }
}
