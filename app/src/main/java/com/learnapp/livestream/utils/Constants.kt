package com.learnapp.livestream.utils

interface Constants {
    companion object {
        const val HTTPS = "https"
        const val EMPTY_STRING = ""
    }

    interface UserMetaData {
        companion object {
            const val DEFAULT_USER_NAME = "Learner"
            const val BUNDLE_KEY = "live_stream_data"

            // do not delete this constant. this is being used in main application
            const val USER_META_DATA = "user_meta_data"
        }
    }

    interface StatusCode {
        companion object {
            const val NO_CONTENT = 204
            const val SUCCESS = 200
        }
    }
}
