package com.learnapp.livestream.data.network.remote

import com.learnapp.livestream.data.models.TokenResponseDto

interface IRemoteOperations {

    suspend fun getLiveStreamApiToken(
        workshopId: String,
        lessonId: String,
        topicId: String
    ): Resource<TokenResponseDto>
}
