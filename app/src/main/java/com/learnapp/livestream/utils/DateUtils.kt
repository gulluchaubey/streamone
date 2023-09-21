package com.learnapp.livestream.utils

import android.content.Context
import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class DateUtils {
    companion object {
        private const val DATE_FORMAT_1 = "EEE, d MMM HH:mm"
        private const val TIME_FORMAT_24_HOUR = "HH:mm"
        private const val TIME_FORMAT_12_HOUR = "h:mm a"

        fun getMessageDate(time: Long, context: Context): String {
            val format = if (DateFormat.is24HourFormat(context)) {
                TIME_FORMAT_24_HOUR
            } else {
                TIME_FORMAT_12_HOUR
            }
            val dateFormat = SimpleDateFormat(format, Locale.getDefault())
            return dateFormat.format(time)
        }
    }
}
