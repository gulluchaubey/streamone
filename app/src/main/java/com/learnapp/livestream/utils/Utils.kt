package com.learnapp.livestream.utils

import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import com.learnapp.livestream.R
import com.learnapp.livestream.data.models.ApiResponseError
import com.learnapp.livestream.data.network.remote.Resource
import timber.log.Timber
import java.io.IOException

private val gson = Gson()

private const val TAG = "Utils"

fun View.visible(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}

fun ImageView.loadImageFromUrl(imageUrl: String?) {
    val imgUri = imageUrl?.toUri()?.buildUpon()?.scheme(Constants.HTTPS)?.build()
    Glide.with(context).load(imgUri)
        .apply(
            RequestOptions()
                .placeholder(R.drawable.anim_loading)
                .error(R.drawable.ic_ls_profile_photo)
        )
        .into(this)
}

fun <T> getObjectFromJson(type: Class<T>, data: String?): T? {
    var result: T? = null
    data?.let {
        try {
            result = gson.fromJson(it, type)
        } catch (exception: JsonSyntaxException) {
            Timber.tag(TAG).e("getObjectFromJson() $exception")
        } catch (exception: JsonParseException) {
            Timber.tag(TAG).e("getObjectFromJson() $exception")
        }
    }
    return result
}

fun Fragment.handleApiError(
    failure: Resource.Error,
    retry: (() -> Unit)? = null
) {
    try {
        when {
            failure.isNetworkError -> requireView().snackbar(
                ApiResponseError.CHECK_INTERNET_CONNECTION.errorName,
                retry
            )
            else -> requireView().snackbar(ApiResponseError.SOMETHING_WENT_WRONG.errorName)
        }
    } catch (exception: IllegalStateException) {
        Timber.e("$exception")
    } catch (exception: IllegalArgumentException) {
        Timber.e("$exception")
    }
}

fun View.snackbar(message: String, action: (() -> Unit)? = null) {
    val snackbar = Snackbar.make(this, message, Snackbar.LENGTH_LONG)
    action?.let {
        snackbar.setAction("Retry") {
            it()
        }
    }
    snackbar.show()
}

fun View.snackbar(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_LONG).show()
}

fun getJsonFromAsset(context: Context, fileName: String): String? {
    val jsonString: String
    try {
        jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (ioException: IOException) {
        Timber.e(ioException.toString())
        return null
    }
    return jsonString
}
