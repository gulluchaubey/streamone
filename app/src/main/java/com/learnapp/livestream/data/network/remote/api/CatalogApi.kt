package com.learnapp.livestream.data.network.remote.api

import com.learnapp.livestream.data.models.TokenResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface CatalogApi {

    @GET("/player/token")
    suspend fun getLiveStreamApiToken(
        @Query("workshopId") workshopId: String,
        @Query("lessonId")lessonId: String,
        @Query("topicId")topicId: String
    ): TokenResponseDto
}
