package com.learnapp.livestream.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.learnapp.livestream.data.network.remote.Resource
import com.learnapp.livestream.data.preferences.UserSharedPreference
import javax.inject.Inject

open class BaseViewModel : ViewModel() {

    companion object {
        private const val TAG = "BaseViewModel"
    }

    @Inject
    lateinit var userSharedPreference: UserSharedPreference

    private val _resourceStatus = MutableLiveData<Resource<Any>?>()
    val resourceStatus: LiveData<Resource<Any>?>
        get() = _resourceStatus

    fun setResourceStatus(resourceStatus: Resource<Any>?) {
        _resourceStatus.postValue(resourceStatus)
    }

    fun setResourceError(exception: String) {
        _resourceStatus.postValue(
            Resource.Error(
                false,
                null,
                null,
                exception
            )
        )
    }
}
