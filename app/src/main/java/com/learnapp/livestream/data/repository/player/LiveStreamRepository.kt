package com.learnapp.livestream.data.repository.player

import com.learnapp.livestream.data.models.TokenResponseDto
import com.learnapp.livestream.data.network.remote.IRemoteOperations
import com.learnapp.livestream.data.network.remote.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class LiveStreamRepository @Inject constructor(
    private val remoteOperations: IRemoteOperations
) {

    fun getToken(workshopId: String, lessonId: String, topicId: String): Flow<Resource<TokenResponseDto>> {
        return flow {
            val response = remoteOperations.getLiveStreamApiToken(workshopId, lessonId, topicId)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }
}
