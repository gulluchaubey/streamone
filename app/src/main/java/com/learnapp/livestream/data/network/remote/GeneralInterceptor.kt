package com.learnapp.livestream.data.network.remote

import com.learnapp.livestream.utils.Constants
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneralInterceptor @Inject constructor() : Interceptor {

    companion object {
        private const val TEXT_PLAIN_TYPE = "text/plain"
        private const val EMPTY_STRING = ""
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == Constants.StatusCode.NO_CONTENT) {
            val emptyBody = EMPTY_STRING.toResponseBody(TEXT_PLAIN_TYPE.toMediaType())
            return response
                .newBuilder()
                .code(Constants.StatusCode.SUCCESS)
                .body(emptyBody)
                .build()
        }
        return response
    }
}
