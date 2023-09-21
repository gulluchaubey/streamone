package com.learnapp.livestream.data.network.remote

import com.learnapp.livestream.BuildConfig
import com.learnapp.livestream.data.preferences.UserSharedPreference
import com.learnapp.livestream.utils.Constants
import com.localebro.okhttpprofiler.OkHttpProfilerInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteDataSource @Inject constructor(
    private val networkInterceptor: NetworkConnectionInterceptor,
    private val generalInterceptor: GeneralInterceptor,
    private val userSharedPreference: UserSharedPreference
) {

    private fun getOkHttpClient(apiKey: String, token: String): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .also {
                            if (token.isNotEmpty() && apiKey.isNotEmpty()) {
                                it.addHeader(
                                    "Authorization",
                                    "Bearer $token"
                                )
                            }
                            if (apiKey.isNotEmpty()) {
                                it.addHeader("x-user-device", "ANDROID")
                                it.addHeader("x-api-key", apiKey)
                                it.addHeader("content-type", "application/json")
                            }
                        }.build()
                )
            }
            .addInterceptor(generalInterceptor)
            .addInterceptor(networkInterceptor)
            .also { client ->
                if (BuildConfig.DEBUG) {
                    val logging = HttpLoggingInterceptor()
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY)
                    client.addInterceptor(OkHttpProfilerInterceptor())
                }
            }.build()
    }

    fun <Api> buildApi(api: Class<Api>, apiKey: String, baseUrl: String, token: String): Api {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(getOkHttpClient(apiKey, token))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(api)
    }

    fun <Api> buildApi(api: Class<Api>): Api {
        val userMetaData = userSharedPreference.getUserMetaData()
        return Retrofit.Builder()
            .baseUrl(userMetaData?.apiUrl ?: Constants.EMPTY_STRING)
            .client(
                getOkHttpClient(
                    userMetaData?.apiKey ?: Constants.EMPTY_STRING,
                    userMetaData?.accessToken ?: Constants.EMPTY_STRING
                )
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(api)
    }
}
