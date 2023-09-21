package com.learnapp.livestream.ui.player.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.learnapp.livestream.data.models.TokenResponseDto
import com.learnapp.livestream.data.models.TrackPeerMap
import com.learnapp.livestream.data.models.UserMetaDataDto
import com.learnapp.livestream.data.network.remote.Resource
import com.learnapp.livestream.data.repository.player.LiveStreamRepository
import com.learnapp.livestream.ui.player.model.ApiErrorCodes
import com.learnapp.livestream.ui.player.model.ChatMessage
import com.learnapp.livestream.ui.player.model.NetworkQualityCode
import com.learnapp.livestream.ui.player.model.RoomStatus
import com.learnapp.livestream.ui.player.model.UserRole
import com.learnapp.livestream.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import live.hms.video.error.ErrorCodes
import live.hms.video.error.HMSException
import live.hms.video.media.tracks.HMSTrack
import live.hms.video.media.tracks.HMSVideoTrack
import live.hms.video.sdk.HMSPreviewListener
import live.hms.video.sdk.HMSSDK
import live.hms.video.sdk.HMSUpdateListener
import live.hms.video.sdk.models.HMSConfig
import live.hms.video.sdk.models.HMSMessage
import live.hms.video.sdk.models.HMSPeer
import live.hms.video.sdk.models.HMSRemovedFromRoom
import live.hms.video.sdk.models.HMSRoleChangeRequest
import live.hms.video.sdk.models.HMSRoom
import live.hms.video.sdk.models.enums.HMSPeerUpdate
import live.hms.video.sdk.models.enums.HMSRoomUpdate
import live.hms.video.sdk.models.enums.HMSTrackUpdate
import live.hms.video.sdk.models.trackchangerequest.HMSChangeTrackStateRequest
import live.hms.video.utils.toJson
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val repository: LiveStreamRepository
) :
    ChatViewModel() {

    companion object {
        private const val TAG = "PlayerViewModel"
        private const val AVATAR_URL_KEY = "avatarUrl"
    }

    private val _peerList = MutableLiveData<List<TrackPeerMap>>(emptyList())
    val peerList: LiveData<List<TrackPeerMap>> = _peerList

    private val _isRoomEnded = MutableLiveData<Boolean>()
    val isRoomEnded: LiveData<Boolean>
        get() = _isRoomEnded

    private val _networkQualityCode = MutableLiveData<NetworkQualityCode>()
    val networkQualityCode: LiveData<NetworkQualityCode>
        get() = _networkQualityCode

    private val _roomStatus = MutableLiveData<RoomStatus>()
    val roomStatus: LiveData<RoomStatus>
        get() = _roomStatus

    private val _isJoined = MutableLiveData<Boolean?>()
    val isJoined: LiveData<Boolean?>
        get() = _isJoined

    private val _errorCode = MutableLiveData<ApiErrorCodes?>()
    val errorCode: LiveData<ApiErrorCodes?>
        get() = _errorCode

    private var hmsConfig: HMSConfig? = null

    private val previewListener = object : HMSPreviewListener {
        override fun onError(error: HMSException) {
            // 400 for room disabled
            // 401 for wrong token
            Timber.tag(TAG).e("Preview onError $error")
            onErrorReceived(error)
        }

        override fun onPeerUpdate(type: HMSPeerUpdate, peer: HMSPeer) {
            // peer.networkQuality -1 for room disabled and wrong token or
            // no internet connectivity
            Timber.tag(TAG).v("Preview onPeerUpdate type - $type peer ${peer.networkQuality}")
            if (type == HMSPeerUpdate.NETWORK_QUALITY_UPDATED && peer.networkQuality?.downlinkQuality != null) {
                NetworkQualityCode.values().firstOrNull { it.code == peer.networkQuality?.downlinkQuality }?.let {
                        networkQualityCode ->
                    _networkQualityCode.postValue(networkQualityCode)
                    if (errorCode.value == null &&
                        (
                                networkQualityCode == NetworkQualityCode.TEST_TIMEOUT ||
                                        networkQualityCode == NetworkQualityCode.NETWORK_FAILURE
                                )
                    ) {
                        _errorCode.postValue(ApiErrorCodes.NO_INTERNET_CONNECTIVITY)
                    }
                }
            }
        }

        override fun onPreview(room: HMSRoom, localTracks: Array<HMSTrack>) {
            Timber.tag(TAG).v("Preview onPreview ")
            clearErrorCode()
            joinRoom()
        }

        override fun onRoomUpdate(type: HMSRoomUpdate, hmsRoom: HMSRoom) {
            Timber.tag(TAG).v("Preview onRoomUpdate ")
        }
    }

    private val hmsUpdateListener = object : HMSUpdateListener {
        override fun onChangeTrackStateRequest(details: HMSChangeTrackStateRequest) {
            Timber.tag(TAG).v("onChangeTrackStateRequest()")
        }

        override fun onError(error: HMSException) {
            Timber.tag(TAG).e("onError() $error")
            onErrorReceived(error)
        }

        override fun onJoin(room: HMSRoom) {
            Timber.tag(TAG).v("onJoin()")
            _isJoined.postValue(true)
            filterVideoTracks()
        }

        override fun onMessageReceived(message: HMSMessage) {
            Timber.tag(TAG).v("onMessageReceived()")
            receivedMessage(
                ChatMessage(
                    message,
                    false
                )
            )
        }

        override fun onPeerUpdate(type: HMSPeerUpdate, peer: HMSPeer) {
            Timber.tag(TAG).v("onPeerUpdate() type - $type peer ${peer.networkQuality}")
            if (type == HMSPeerUpdate.NETWORK_QUALITY_UPDATED && peer.networkQuality?.downlinkQuality != null) {
                NetworkQualityCode.values().firstOrNull { it.code == peer.networkQuality?.downlinkQuality }?.let {
                        networkQualityCode ->
                    _networkQualityCode.postValue(networkQualityCode)
                }
            }
            filterVideoTracks()
        }

        override fun onRoleChangeRequest(request: HMSRoleChangeRequest) {
            Timber.tag(TAG).v("onRoleChangeRequest()")
        }

        override fun onRoomUpdate(type: HMSRoomUpdate, hmsRoom: HMSRoom) {
            Timber.tag(TAG).v("onRoomUpdate()")
//        if (type == HMSRoomUpdate.ROOM_PEER_COUNT_UPDATED) {
//            updatePeerCount(hmsRoom)
//        }
        }

        override fun onTrackUpdate(type: HMSTrackUpdate, track: HMSTrack, peer: HMSPeer) {
            Timber.tag(TAG).v("onTrackUpdate()")
            if (type == HMSTrackUpdate.TRACK_DEGRADED) {
                _roomStatus.postValue(RoomStatus.TRACK_DEGRADED)
            } else if (type == HMSTrackUpdate.TRACK_RESTORED) {
                _roomStatus.postValue(RoomStatus.TRACK_RESTORED)
            }
            filterVideoTracks()
        }

        override fun onReconnected() {
            super.onReconnected()
            Timber.tag(TAG).v("onReconnected")
            _roomStatus.postValue(RoomStatus.RECONNECTED)
//            if (roomStatus.value != RoomStatus.RECONNECTED) {
//                Timber.tag(TAG).v("onReconnected set connection back flag")
//                _roomStatus.postValue(RoomStatus.RECONNECTED)
//            }
        }

        override fun onReconnecting(error: HMSException) {
            super.onReconnecting(error)
            Timber.tag(TAG).e("onReconnecting $error")
            if (ErrorCodes.WebSocketConnectionErrors.cWebSocketConnectionLost == error.code &&
                roomStatus.value != RoomStatus.RECONNECTING
            ) {
                Timber.tag(TAG).e("onReconnecting set no connection flag")
                _roomStatus.postValue(RoomStatus.RECONNECTING)
            }
        }

        override fun onRemovedFromRoom(notification: HMSRemovedFromRoom) {
            super.onRemovedFromRoom(notification)
            Timber.tag(TAG).v("onRemovedFromRoom")
            onRoomEnd(notification)
        }
    }

    fun initLiveStream(application: Application) {
        Timber.tag(TAG).v("extractBundle()")
        userSharedPreference.getUserMetaData()?.let {
            requestToken(it, application)
        }
    }

    private fun requestToken(userMetaDataDto: UserMetaDataDto, application: Application) {
        Timber.tag(TAG).v("requestToken()")
        viewModelScope.launch {
            setResourceStatus(Resource.Loading(true))
            repository.getToken(userMetaDataDto.workshopId, userMetaDataDto.lessonId, userMetaDataDto.topicId)
                .catch { exception ->
                    Timber.tag(TAG).e("requestToken $exception")
                    setResourceError("$exception")
                }
                .collect {
                    setRequestTokenStatus(it, userMetaDataDto, application)
                }
        }
    }

    private fun setRequestTokenStatus(
        resource: Resource<TokenResponseDto>,
        userMetaDataDto: UserMetaDataDto,
        application: Application
    ) {
        Timber.tag(TAG).v("setRequestTokenStatus()")
        setResourceStatus(resource)
        if (resource is Resource.Success) {
            joinPreview(resource.data.token, userMetaDataDto, application)
        }
    }

    private fun joinPreview(
        token: String,
        userMetaDataDto: UserMetaDataDto,
        application: Application
    ) {
        Timber.tag(TAG).v("joinPreview()")
        val userName = userMetaDataDto.username ?: Constants.UserMetaData.DEFAULT_USER_NAME

        val metadata = userMetaDataDto.avatarUrl?.let {
            val metadataJson = JsonObject()
            metadataJson.addProperty(AVATAR_URL_KEY, it)
            metadataJson.toJson()
        } ?: Constants.EMPTY_STRING

        val hmsSDK = HMSSDK
            .Builder(application)
            .build()

        val hmsConfig = HMSConfig(userName, token, metadata = metadata, true)
        this.hmsSDK = hmsSDK
        this.hmsConfig = hmsConfig
        hmsSDK.preview(hmsConfig, previewListener)
    }

    private fun joinRoom() {
        hmsConfig?.let {
            hmsSDK?.join(it, hmsUpdateListener)
        }
    }

    private fun filterVideoTracks() {
        Timber.tag(TAG).v("filterVideoTracks()")

        val videoPeerList = hmsSDK?.getPeers()?.filter { hmsPeer ->
            UserRole.values().firstOrNull { it.role == hmsPeer.hmsRole.name } != null
        }

        val peerTrackList = mutableListOf<TrackPeerMap>()

        val screenSharePeer = videoPeerList?.firstOrNull {
            val screenShare = it.auxiliaryTracks.find { auxTrack -> auxTrack is HMSVideoTrack }
            if (screenShare is HMSVideoTrack) {
                peerTrackList.add(TrackPeerMap(screenShare, it))
                Timber.tag(TAG).v("screenShareTrack Peer $it")
                Timber.tag(TAG).v("screenShareTrack $screenShare")
            }
            screenShare is HMSVideoTrack
        }

        if (screenSharePeer == null) {
            // admin
            videoPeerList?.firstOrNull { it.hmsRole.name == UserRole.ADMIN.role }?.let {
                peerTrackList.add(TrackPeerMap(it.videoTrack, it))
                Timber.tag(TAG).v("admin Peer $it")
            }

            // mentor
            videoPeerList?.filter { peer ->
                peer.hmsRole.name == UserRole.MENTOR.role && peer.audioTrack?.isMute == false
            }?.forEach { peer ->
                peerTrackList.add(TrackPeerMap(peer.videoTrack, peer))
                Timber.tag(TAG).v("mentor audioTrack mute ${peer.audioTrack?.isMute}")
            }
        }

        _peerList.postValue(peerTrackList)
    }

    private fun onRoomEnd(hmsRemovedFromRoom: HMSRemovedFromRoom) {
        Timber.tag(TAG).v("onRoomEnd $hmsRemovedFromRoom")
        if (hmsRemovedFromRoom.roomWasEnded ||
            hmsSDK?.getLocalPeer()?.peerID == hmsRemovedFromRoom.peerWhoRemoved?.peerID
        ) {
            clearMessages()
            _isRoomEnded.postValue(true)
        }
    }

    fun clearJoinFlag() {
        _isJoined.postValue(null)
    }

    private fun onErrorReceived(error: HMSException) {
        Timber.tag(TAG).e("onErrorReceived $error")
        ApiErrorCodes.values().firstOrNull { it.code == error.code }?.let {
            _errorCode.postValue(it)
        }
    }

    private fun clearErrorCode() {
        _errorCode.postValue(null)
    }

//    private fun updatePeerCount(hmsRoom: HMSRoom) {
//        Timber.tag(TAG).v("updatePeerCount() Peer Count ${hmsRoom.peerCount}")
//        val count = if (hmsRoom.peerCount > DEFAULT_PEER_COUNT) {
//            hmsRoom.peerCount
//        } else {
//            DEFAULT_PEER_COUNT
//        }
//        _peerCount.postValue(count)
//    }
}
