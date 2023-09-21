package com.learnapp.livestream.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import com.learnapp.livestream.data.models.UserMetaDataDto
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSharedPreference @Inject constructor(
    @ApplicationContext context: Context,
    private val gson: Gson
) {

    companion object {
        private const val LIVE_STREAM_PREF = "live_stream_pref"
        private const val TAG = "UserSharedPreference"
        const val USER_META_DATA = "user_meta_data"
    }

    private val sharedPreferences: SharedPreferences

    init {
        val masterKeyAlias: String = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        sharedPreferences = EncryptedSharedPreferences.create(
            LIVE_STREAM_PREF,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveStringParam(value: String, key: String) {
        sharedPreferences.edit()
            .putString(key, value)
            .apply()
    }

    fun getUserMetaData(): UserMetaDataDto? {
        val metaDataString = sharedPreferences.getString(USER_META_DATA, null)
        var userMetaDataDto: UserMetaDataDto? = null
        metaDataString?.let { metaData ->
            try {
                userMetaDataDto = gson.fromJson(metaData, UserMetaDataDto::class.java)
            } catch (exception: JsonSyntaxException) {
                Timber.tag(TAG).e("getUserMetaData $exception")
            } catch (exception: JsonParseException) {
                Timber.tag(TAG).e("getUserMetaData $exception")
            }
        }
        return userMetaDataDto
    }

    fun getStringParam(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    fun clearAll() {
        Timber.tag(TAG).v("clearAll()")
        val editor = sharedPreferences.edit()
        sharedPreferences.all.keys.forEach { key ->
            editor.remove(key)
        }
        editor.apply()
    }
}
