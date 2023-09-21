package com.learnapp.livestream.ui.player.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.learnapp.livestream.ui.base.BaseViewModel
import com.learnapp.livestream.ui.player.adapter.Recipient
import com.learnapp.livestream.ui.player.model.ChatMessage
import live.hms.video.error.HMSException
import live.hms.video.sdk.HMSMessageResultListener
import live.hms.video.sdk.HMSSDK
import live.hms.video.sdk.models.HMSMessage
import live.hms.video.sdk.models.enums.HMSMessageType
import timber.log.Timber

open class ChatViewModel : BaseViewModel() {

    companion object {
        private const val TAG = "ChatViewModel"
        const val DEFAULT_PEER_COUNT = 0
        const val DEFAULT_NAME = "You"
        const val COUNT_ZERO = 0
        const val COUNT_ONE = 1
    }

    protected var hmsSDK: HMSSDK? = null

    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>>
        get() = _messages

    private val _unreadMessagesCount = MutableLiveData(0)
    val unreadMessagesCount: LiveData<Int>
        get() = _unreadMessagesCount

    private val currentSelectedRecipient: Recipient = Recipient.Everyone

    private val _isChatWindowHidden = MutableLiveData<Boolean?>()
    val icChatWindowHidden: LiveData<Boolean?>
        get() = _isChatWindowHidden

//    protected val _peerCount = MutableLiveData(DEFAULT_PEER_COUNT)
//    val peerCount: LiveData<Int>
//        get() = _peerCount

//    fun sendMessage(messageStr: String) {
//        val message = ChatMessage(
//            DEFAULT_NAME,
//            System.currentTimeMillis(),
//            messageStr,
//            true,
//            Recipient.Everyone
//        )
//
//        when (val recipient = currentSelectedRecipient) {
//            Recipient.Everyone -> broadcast(message)
//            is Recipient.Peer -> directMessage(
//                message.copy(recipient = Recipient.Peer(recipient.peer)),
//                recipient.peer
//            )
//            is Recipient.Role -> groupMessage(
//                message.copy(recipient = Recipient.Role(recipient.role)),
//                recipient.role
//            )
//        }
//    }

//    private fun directMessage(message: ChatMessage, peer: HMSPeer) {
//        hmsSDK?.sendDirectMessage(
//            message.message,
//            HMSMessageType.CHAT,
//            peer,
//            object :
//                HMSMessageResultListener {
//                override fun onError(error: HMSException) {
//                    Timber.tag(TAG).e("directMessage $error")
//                }
//
//                override fun onSuccess(hmsMessage: HMSMessage) {
//                    addMessage(ChatMessage(hmsMessage, true))
//                }
//            }
//        )
//    }
//
//    private fun groupMessage(message: ChatMessage, role: HMSRole) {
//        hmsSDK?.sendGroupMessage(
//            message.message,
//            HMSMessageType.CHAT,
//            listOf(role),
//            object :
//                HMSMessageResultListener {
//                override fun onError(error: HMSException) {
//                    Timber.tag(TAG).e("groupMessage $error")
//                }
//
//                override fun onSuccess(hmsMessage: HMSMessage) {
//                    addMessage(ChatMessage(hmsMessage, true))
//                }
//            }
//        )
//    }

    fun sendMessage(messageStr: String) {
        hmsSDK?.sendBroadcastMessage(
            messageStr,
            HMSMessageType.CHAT,
            object :
                HMSMessageResultListener {
                override fun onError(error: HMSException) {
                    Timber.tag(TAG).e("sendMessage $error")
                }

                override fun onSuccess(hmsMessage: HMSMessage) {
                    addMessage(ChatMessage(hmsMessage, true))
                }
            }
        )
    }

    fun clearMessages() {
        _messages.postValue(emptyList())
        _unreadMessagesCount.postValue(COUNT_ZERO)
    }

    fun clearMessageReceivedCount() {
        _unreadMessagesCount.postValue(COUNT_ZERO)
    }

    private fun addMessage(message: ChatMessage) {
        Timber.tag(TAG).v("addMessage() $message")
        messages.value?.plus(message)?.let {
            _messages.postValue(it)
        }
    }

    fun receivedMessage(message: ChatMessage) {
        Timber.tag(TAG).v("receivedMessage $message")
        _unreadMessagesCount.postValue(unreadMessagesCount.value?.plus(COUNT_ONE))
        addMessage(message)
    }

    fun closeChatWindow(isClosed: Boolean?) {
        _isChatWindowHidden.value = isClosed
    }

    fun getHmsSdk() = hmsSDK
}
