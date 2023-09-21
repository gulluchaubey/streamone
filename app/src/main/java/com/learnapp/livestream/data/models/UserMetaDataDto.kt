package com.learnapp.livestream.data.models

data class UserMetaDataDto(
    val username: String?,
    val avatarUrl: String?,
    val workshopId: String,
    val accessToken: String,
    val apiKey: String,
    val apiUrl: String,
    val lessonId: String,
    val topicId: String
)
