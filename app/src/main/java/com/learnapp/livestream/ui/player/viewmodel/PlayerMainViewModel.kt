package com.learnapp.livestream.ui.player.viewmodel

import android.os.Bundle
import com.learnapp.livestream.data.preferences.UserSharedPreference
import com.learnapp.livestream.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PlayerMainViewModel @Inject constructor() : BaseViewModel() {

    companion object {
        private const val TAG = "PlayerMainViewModel"
    }

    fun extractBundle(bundle: Bundle?): Boolean {
        Timber.tag(TAG).v("extractBundle")
        var isValidUserData = false
        bundle?.getString(UserSharedPreference.USER_META_DATA)?.let {
            userSharedPreference.saveStringParam(
                it,
                UserSharedPreference.USER_META_DATA
            )
            isValidUserData = true
        }
        return isValidUserData
    }
}
