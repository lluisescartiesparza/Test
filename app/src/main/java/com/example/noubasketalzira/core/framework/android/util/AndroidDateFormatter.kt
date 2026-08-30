package com.example.noubasketalzira.core.framework.android.util

import com.example.noubasketalzira.core.domain.util.IDateFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AndroidDateFormatter : IDateFormatter {
    override fun formatTimestamp(timestamp: Long, pattern: String): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
    }

    override fun parseIso8601ToTimestamp(isoString: String): Long {
        return try {
            val cleanString = isoString.substringBefore(".").substringBefore("+").substringBefore("Z") + "Z"
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            format.timeZone = java.util.TimeZone.getTimeZone("UTC")
            format.parse(cleanString)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}
