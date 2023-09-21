package com.learnapp.livestream.data.network.remote

import com.learnapp.livestream.data.models.TokenResponseDto
import com.learnapp.livestream.data.network.remote.api.CatalogApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteOperations @Inject constructor(
    private val catalogApi: CatalogApi
) : IRemoteOperations, SafeApiCall {

    override suspend fun getLiveStreamApiToken(
        workshopId: String,
        lessonId: String,
        topicId: String
    ): Resource<TokenResponseDto> {
        return safeApiCall { catalogApi.getLiveStreamApiToken(workshopId, lessonId, topicId) }
    }
}
