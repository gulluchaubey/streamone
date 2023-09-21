package com.learnapp.livestream.ui.player.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.learnapp.livestream.databinding.FragmentChatBinding
import com.learnapp.livestream.ui.base.BaseFragment
import com.learnapp.livestream.ui.player.adapter.ChatAdapter
import com.learnapp.livestream.ui.player.model.ChatMessage
import com.learnapp.livestream.ui.player.viewmodel.PlayerViewModel
import com.learnapp.livestream.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ChatFragment : BaseFragment(), View.OnClickListener {

    companion object {
        private const val TAG = "ChatFragment"
        private const val SCROLL_DIRECTION_UP = 1
    }

    private var _fragmentBinding: FragmentChatBinding? = null
    private val fragmentBinding get() = _fragmentBinding!!

    private val viewModel: PlayerViewModel by activityViewModels()
    private var chatAdapter: ChatAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _fragmentBinding = FragmentChatBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnViewClickListeners()
        initChatRecyclerView()
        initSendButton()
        setObserver()
    }

    private fun setOnViewClickListeners() {
        fragmentBinding.closeImageView.setOnClickListener(this)
        fragmentBinding.pullDownImageView.setOnClickListener(this)
    }

    private fun initChatRecyclerView() {
        val messageList = viewModel.messages.value ?: emptyList()
        fragmentBinding.pullDownImageView.visible(false)
        chatAdapter = ChatAdapter(messageList.toMutableList())
        fragmentBinding.chatRecyclerView.apply {
            adapter = chatAdapter
            scrollToPosition(messageList.size - 1)
        }

        fragmentBinding.chatRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                Timber.tag(TAG).v("onScrollStateChanged $newState")
                if (!fragmentBinding.chatRecyclerView.canScrollVertically(SCROLL_DIRECTION_UP) &&
                    newState == RecyclerView.SCROLL_STATE_IDLE
                ) {
                    fragmentBinding.pullDownImageView.visible(false)
                }
                if (fragmentBinding.chatRecyclerView.canScrollVertically(SCROLL_DIRECTION_UP)) {
                    fragmentBinding.pullDownImageView.visible(true)
                }
            }
        })
    }

    private fun setObserver() {
        viewModel.messages.observe(viewLifecycleOwner) { onMessageReceived(it) }
//        viewModel.peerCount.observe(viewLifecycleOwner) { showPeerCount(it) }
    }

//    private fun showPeerCount(peerCount: Int) {
//        val count = "$peerCount Watching"
//        fragmentBinding.userCountTextView.text = count
//    }

    private fun onMessageReceived(messageList: List<ChatMessage>) {
        chatAdapter?.setNewMessages(messageList)
        scrollChatAdapterToBottom()
    }

    private fun initSendButton() {
        fragmentBinding.messageTextInputLayout.setEndIconOnClickListener {
            val messageStr = fragmentBinding.messageEditText.text.toString().trim()
            if (messageStr.isNotEmpty()) {
                viewModel.sendMessage(messageStr)
                fragmentBinding.messageEditText.setText("")
            }
        }
    }

    override fun onClick(view: View) {
        if (view.id == fragmentBinding.closeImageView.id) {
            onCloseClick()
        } else if (view.id == fragmentBinding.pullDownImageView.id) {
            scrollChatAdapterToBottom()
        }
    }

    private fun scrollChatAdapterToBottom() {
        fragmentBinding.chatRecyclerView.apply {
            viewModel.messages.value?.size?.let {
                scrollToPosition(it - 1)
            }
        }
        fragmentBinding.pullDownImageView.visible(false)
    }

    private fun onCloseClick() {
        viewModel.closeChatWindow(true)
        fragmentBinding.messageEditText.clearFocus()
        fragmentBinding.messageEditText.onEditorAction(EditorInfo.IME_ACTION_DONE)
    }
}
